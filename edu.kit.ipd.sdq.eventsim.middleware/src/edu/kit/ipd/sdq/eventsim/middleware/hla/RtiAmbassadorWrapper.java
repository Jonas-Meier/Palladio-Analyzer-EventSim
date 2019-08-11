package edu.kit.ipd.sdq.eventsim.middleware.hla;

import java.io.File;
import java.net.URL;

import org.apache.log4j.varia.DenyAllFilter;

import edu.kit.ipd.sdq.eventsim.middleware.hla.adaption.AbstractConverter;
import edu.kit.ipd.sdq.eventsim.middleware.hla.adaption.EncoderService;
import edu.kit.ipd.sdq.eventsim.middleware.hla.adaption.Integer32Converter;
import edu.kit.ipd.sdq.eventsim.middleware.hla.adaption.StringConverter;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;

public class RtiAmbassadorWrapper {

	private RTIambassador rtiambassador;
	private EncoderFactory encoderFactory;
	
	private RtiDataContainer rtiData;
	
	private InteractionClassHandle eventTriggeredHandle;
	private ParameterHandle eventNameHandle;
	
	public RtiAmbassadorWrapper(RtiDataContainer rtiData) throws RTIinternalError {
		this.rtiData=rtiData;
		
		rtiambassador = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		
		setupAdaption();
	}
	
	public void connectAndJoin(MiddlewareFederateAmbassador fedamb, String fedName) {
		try {
			rtiambassador.connect(fedamb, EventSimHLAvalues.callbackMode);
			
			URL[] fom = new URL[] { (new File("FOMs/EventSimFOM.xml")).toURI().toURL() };
			
			try {
				rtiambassador.createFederationExecution("EventSimMiddleware", fom);
			} catch (FederationExecutionAlreadyExists ignore) {}
			
			rtiambassador.joinFederationExecution(fedName, "EventSimMiddleware", "EventSimMiddleware", fom);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void setupAdaption() {
		rtiData.encoderService = new EncoderService();
		
		AbstractConverter<String> stringConverter = new StringConverter(encoderFactory);
		AbstractConverter<Integer> integer32Converter = new Integer32Converter(encoderFactory);
		
		rtiData.encoderService.addConverter(stringConverter);
		rtiData.encoderService.addConverter(integer32Converter);
	}
	
	public void publish() {
		try {
			eventTriggeredHandle = rtiambassador.getInteractionClassHandle("HLAinteractionRoot.EventTriggered");
			rtiambassador.publishInteractionClass(eventTriggeredHandle);
			eventNameHandle = rtiambassador.getParameterHandle(eventTriggeredHandle, "EventName");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void subscribe() {
		
	}
	
}
