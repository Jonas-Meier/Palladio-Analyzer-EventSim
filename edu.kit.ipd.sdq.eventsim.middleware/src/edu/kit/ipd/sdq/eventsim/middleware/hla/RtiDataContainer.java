package edu.kit.ipd.sdq.eventsim.middleware.hla;

import edu.kit.ipd.sdq.eventsim.middleware.hla.adaption.EncoderService;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;

public class RtiDataContainer {
	
	protected EncoderService encoderService = new EncoderService();
	
	protected InteractionClassHandle eventTriggeredHandle;
	protected ParameterHandle eventNameHandle;
}
