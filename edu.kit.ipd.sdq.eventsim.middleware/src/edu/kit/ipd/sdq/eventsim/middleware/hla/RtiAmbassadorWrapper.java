package edu.kit.ipd.sdq.eventsim.middleware.hla;

import edu.kit.ipd.sdq.eventsim.middleware.hla.adaption.AbstractConverter;
import edu.kit.ipd.sdq.eventsim.middleware.hla.adaption.EncoderService;
import edu.kit.ipd.sdq.eventsim.middleware.hla.adaption.Integer32Converter;
import edu.kit.ipd.sdq.eventsim.middleware.hla.adaption.StringConverter;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIinternalError;

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
	
	public void connectAndJoin() {
		//TODO: 
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
