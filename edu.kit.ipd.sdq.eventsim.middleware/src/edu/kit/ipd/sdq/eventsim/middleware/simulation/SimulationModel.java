package edu.kit.ipd.sdq.eventsim.middleware.simulation;

import de.uka.ipd.sdq.scheduler.SchedulerModel;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationStartEvent;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationStopEvent;

/**
 * The simulation model is the core of an abstract simulation engine based
 * simulation. It controls the simulation process and basically represents the
 * central simulation timeline. For more information see {@code ISimulationModel}
 * 
 * @author Christoph Föhrdes
 */
public class SimulationModel extends SchedulerModel implements ISimulationModel {

	private ISimulationControl control;
	private ISimEngineFactory factory;
	private ISimulationMiddleware middleware;

	public SimulationModel(ISimEngineFactory factory, ISimulationMiddleware middleware) {
		this.factory = factory;
		this.middleware = middleware;
	}

	@Override
	public ISimulationControl getSimulationControl() {
		if (this.control == null) {
			this.control = this.factory.createSimulationControl();
		}
		return this.control;
	}

	@Override
	public void setSimulationControl(ISimulationControl control) {
		this.control = control;
	}

	@Override
	public void setSimulationEngineFactory(ISimEngineFactory factory) {
		this.factory = factory;
	}

	@Override
	public ISimEngineFactory getSimEngineFactory() {
		return this.factory;
	}

	@Override
	public ISimulationConfiguration getConfiguration() {
		return this.middleware.getSimulationConfiguration();
	}

	@Override
	public void init() {	
		this.middleware.triggerEvent(new SimulationStartEvent());
	}

	@Override
	public void finalise() {
		// after the simulation has stopped we trigger the finalize event for cleanup tasks
		this.middleware.triggerEvent(new SimulationStopEvent());
	}

}
