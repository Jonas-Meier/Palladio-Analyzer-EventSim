package edu.kit.ipd.sdq.eventsim.workload.hla;

import edu.kit.ipd.sdq.eventsim.workload.hla.adaption.AbstractConverter;
import edu.kit.ipd.sdq.eventsim.workload.hla.adaption.EncoderService;
import edu.kit.ipd.sdq.eventsim.workload.hla.adaption.Integer32Converter;
import edu.kit.ipd.sdq.eventsim.workload.hla.adaption.StringConverter;

import java.io.File;
import java.net.URL;

import edu.kit.ipd.sdq.eventsim.workload.hla.EventSimHLAvalues;
import edu.kit.ipd.sdq.eventsim.workload.hla.RtiDataContainer;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;

public class RtiAmbassadorWrapper {

	private RTIambassador rtiambassador;
	private EncoderFactory encoderFactory;
	
	private RtiDataContainer rtiData;
	
	public RtiAmbassadorWrapper(RtiDataContainer rtiData) throws RTIinternalError {
		this.rtiData=rtiData;
		
		rtiambassador = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		
		setupAdaption();
	}
	
	
	
	
	public void connectAndJoin(FederateAmbassador fedamb, String fedName) {
		try {
			rtiambassador.connect(fedamb, EventSimHLAvalues.callbackMode);
			
			URL[] fom = new URL[] { (new File("FOMs/EventSimFOM.xml")).toURI().toURL() };
			rtiambassador.joinFederationExecution(fedName, "EventSimWorkload", "EventSimWorkload", fom);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void publish() {
		
	}
	
	public void subscribe() {
		try {
			rtiData.eventTriggeredHandle = rtiambassador.getInteractionClassHandle("HLAinteractionRoot.EventTriggered");
			rtiambassador.subscribeInteractionClass(rtiData.eventTriggeredHandle);
			rtiData.eventNameHandle = rtiambassador.getParameterHandle(rtiData.eventTriggeredHandle, "EventName");
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
}
