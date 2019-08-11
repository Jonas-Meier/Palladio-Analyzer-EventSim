package edu.kit.ipd.sdq.eventsim.workload.hla;


public class CallbackHandler {
	
	public double federateTime = 0.0;
	public int receivedEventCounter=0;
	
	
	public void handleTriggeredEvent(String eventName) {
		//TODO: create event corresponding to the eventName given; then deliver the event to all classes in the workload that subscribe to this event (e.g. SimulationPrepareEvent to the EventSimWorkloadModel)
		
	}
	
}
