package edu.kit.ipd.sdq.eventsim.workload.hla;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.RTIinternalError;

public class RtiAmbassadorWrapper {

	private RTIambassador rtiambassador;
	private RtiDataContainer rtiData;
	
	public RtiAmbassadorWrapper(RtiDataContainer rtiData) throws RTIinternalError {
		this.rtiData = rtiData;
		
		this.rtiambassador = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
	}
	
	
	
	public void publishAndSubscribe() {
		
	}
}
