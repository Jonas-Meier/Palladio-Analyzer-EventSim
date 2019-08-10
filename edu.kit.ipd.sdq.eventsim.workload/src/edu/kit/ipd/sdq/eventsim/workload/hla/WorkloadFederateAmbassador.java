package edu.kit.ipd.sdq.eventsim.workload.hla;

import edu.kit.ipd.sdq.eventsim.workload.hla.CallbackHandler;
import edu.kit.ipd.sdq.eventsim.workload.hla.RtiDataContainer;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.FederateAmbassador.SupplementalReceiveInfo;
import hla.rti1516e.exceptions.FederateInternalError;

public class WorkloadFederateAmbassador extends NullFederateAmbassador{

	private RtiDataContainer rtiData;
	private CallbackHandler cbHandler;
	
	public WorkloadFederateAmbassador(RtiDataContainer rtidata, CallbackHandler cbHandler){
		this.rtiData = rtidata;
		this.cbHandler = cbHandler;
	}
	
	
	@Override
	public void receiveInteraction(
			InteractionClassHandle interactionClass, 
			ParameterHandleValueMap theParameters,
			byte[] userSuppliedTag, 
			OrderType sentOrdering, 
			TransportationTypeHandle theTransport,
			SupplementalReceiveInfo receiveInfo
	) throws FederateInternalError {
		
		this.receiveInteraction(
				interactionClass, 
				theParameters, 
				userSuppliedTag, 
				sentOrdering, 
				theTransport,
				null,
				sentOrdering,
				receiveInfo
		);
	}
	
	@Override
	public void receiveInteraction(
			InteractionClassHandle interactionClass, 
			ParameterHandleValueMap theParameters,
			byte[] userSuppliedTag, 
			OrderType sentOrdering, 
			TransportationTypeHandle theTransport, 
			@SuppressWarnings("rawtypes") LogicalTime theTime,
			OrderType receivedOrdering, 
			MessageRetractionHandle retractionHandle, 
			SupplementalReceiveInfo receiveInfo
	) throws FederateInternalError {
		
		this.receiveInteraction(
				interactionClass, 
				theParameters, 
				userSuppliedTag, 
				sentOrdering, 
				theTransport, 
				theTime,
				receivedOrdering, 
				receiveInfo
		);
	}
	
	
	@Override
	public void receiveInteraction(
			InteractionClassHandle interactionClass, 
			ParameterHandleValueMap theParameters,
			byte[] userSuppliedTag, 
			OrderType sentOrdering, 
			TransportationTypeHandle theTransport, 
			@SuppressWarnings("rawtypes") LogicalTime theTime,
			OrderType receivedOrdering, 
			SupplementalReceiveInfo receiveInfo
	) throws FederateInternalError {
		
		cbHandler.receivedEventCounter++;
		
		if(interactionClass.equals(rtiData.eventTriggeredHandle)) {
			String eventName = rtiData.encoderService.convert(
					String.class.getTypeName(), 
					theParameters.get(rtiData.eventNameHandle)
			);
			//TODO: from here, (maybe invoke the callbackHandler?!) deliver the SImulationPrepareEvent to the EventSimWorkloadModel
		}
		
	}
}
