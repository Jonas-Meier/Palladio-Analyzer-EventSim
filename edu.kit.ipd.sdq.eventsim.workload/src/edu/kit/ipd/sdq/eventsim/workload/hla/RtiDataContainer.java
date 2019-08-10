package edu.kit.ipd.sdq.eventsim.workload.hla;

import edu.kit.ipd.sdq.eventsim.workload.hla.adaption.EncoderService;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;

/**
 * Contains data shared by the RtiAmbassadorWrapper and the WorkloadFederateAmbassador
 * 
 */
public class RtiDataContainer {
	
	protected EncoderService encoderService = new EncoderService();
	
	protected InteractionClassHandle eventTriggeredHandle;
	protected ParameterHandle eventNameHandle;
}
