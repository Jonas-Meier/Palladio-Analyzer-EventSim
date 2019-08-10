package edu.kit.ipd.sdq.eventsim.middleware.hla;

import hla.rti1516e.NullFederateAmbassador;

public class MiddlewareFederateAmbassador extends NullFederateAmbassador{

	private RtiDataContainer rtiData;
	private CallbackHandler cbHandler;
	
	public MiddlewareFederateAmbassador(RtiDataContainer rtidata, CallbackHandler cbHandler) {
		this.rtiData = rtidata;
		this.cbHandler = cbHandler;
	}
	
	
	
}
