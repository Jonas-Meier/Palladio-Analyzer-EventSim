package edu.kit.ipd.sdq.eventsim.system;

import java.util.Map;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.AbstractEventSimModel;
import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationStopEvent;
import edu.kit.ipd.sdq.eventsim.api.events.SystemRequestFinishedEvent;
import edu.kit.ipd.sdq.eventsim.api.events.SystemRequestSpawnEvent;
import edu.kit.ipd.sdq.eventsim.command.action.FindAllActionsByType;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.osgi.BundleProbeLocator;
import edu.kit.ipd.sdq.eventsim.system.calculators.ResponseTimeOfExternalCallsCalculator;
import edu.kit.ipd.sdq.eventsim.system.command.BuildComponentInstances;
import edu.kit.ipd.sdq.eventsim.system.command.FindAssemblyContextForSystemCall;
import edu.kit.ipd.sdq.eventsim.system.command.InstallExternalCallParameterHandling;
import edu.kit.ipd.sdq.eventsim.system.debug.DebugSeffTraversalListener;
import edu.kit.ipd.sdq.eventsim.system.entities.ForkedRequest;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.events.BeginSeffTraversalEvent;
import edu.kit.ipd.sdq.eventsim.system.handler.AfterSystemCallParameterHandler;
import edu.kit.ipd.sdq.eventsim.system.handler.BeforeSystemCallParameterHandler;
import edu.kit.ipd.sdq.eventsim.system.interpreter.SeffBehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.system.interpreter.SeffInterpreterConfiguration;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.AllocationRegistry;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.ComponentInstance;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.SimulatedResourceContainer;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.SimulatedResourceEnvironment;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.commands.BuildResourceAllocation;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.commands.BuildSimulatedResourceEnvironment;


/**
 * The simulation model. This is the central class of an EventSim simulation
 * run. Before the simulation starts, it initialises the simulation in the
 * {@code init()} method. During the simulation, it provides information about
 * the PCM model that is to be simulated, the simulation configuration and the
 * simulation status. Finally, it cleans up after a simulation run in the
 * {finalise()} method.
 * <p>
 * Instances are created by using the static {@code create} method that builds
 * the simulation model in accordance with a specified simulation configuration.
 * 
 * @author Philipp Merkle
 * 
 */
@Singleton
public class EventSimSystemModel extends AbstractEventSimModel implements ISystem {

	private static final Logger logger = Logger.getLogger(EventSimSystemModel.class);

	private SeffBehaviourInterpreter seffInterpreter;

	private SimulatedResourceEnvironment resourceEnvironment;
	private AllocationRegistry resourceAllocation;
	private Map<String, ComponentInstance> componentRegistry;
	
	private MeasurementFacade<SystemMeasurementConfiguration> measurementFacade;
	
	private IActiveResource activeResource;
	private IPassiveResource passiveResource;
	
	@Inject
	public EventSimSystemModel(IActiveResource activeResource, IPassiveResource passiveResource,
			ISimulationMiddleware middleware, MeasurementStorage measurementStorage) {
		super(middleware, measurementStorage);
		this.activeResource = activeResource;
		this.passiveResource = passiveResource;
		init();
	}

	public void init() {		
		// initialise behavior interpreters
		seffInterpreter = new SeffBehaviourInterpreter(new SeffInterpreterConfiguration(), this);

		// install debug traversal listeners, if debugging is enabled
		if (logger.isDebugEnabled()) {
			DebugSeffTraversalListener.install(this.seffInterpreter.getConfiguration());
		}
		
		this.setupMeasurements();

		// initialise resource environment and allocation
		this.resourceEnvironment = this.execute(new BuildSimulatedResourceEnvironment());
		this.resourceAllocation = this.execute(new BuildResourceAllocation(this.resourceEnvironment));

		// initialise component instances
		this.componentRegistry = this.execute(new BuildComponentInstances(this.resourceAllocation));
		
		// install extern call parameter handling
		this.execute(new InstallExternalCallParameterHandling(this.seffInterpreter.getConfiguration()));
		
		registerEventHandler();
	}
	
	

	/**
	 * Handles the simulation of a service call. Service calls are usually
	 * generated by a workload simulation component.
	 * 
	 * @param user
	 *            The user which initiated the call
	 * @param call
	 *            The called service in form of a PCM entry level system call
	 *            action
	 */
	@Override
	public void callService(IUser user, EntryLevelSystemCall call) {
		// find the component which provides the call
		final AssemblyContext assemblyCtx = this.execute(new FindAssemblyContextForSystemCall(call));
		final ComponentInstance component = this.getComponent(assemblyCtx);
		final OperationSignature signature = call.getOperationSignature__EntryLevelSystemCall();
	
		// spawn a new EventSim request
		final Request request = new Request(this, call, user);
	
		new BeginSeffTraversalEvent(this, component, signature, user.getStochasticExpressionContext()).schedule(request,
				0);
	}

	@Override
	public void finalise() {
		super.finalise();
		
		seffInterpreter.getConfiguration().removeTraversalListeners();
		
		measurementFacade = null;
	}
	
	public IActiveResource getActiveResource() {
		return activeResource;
	}
	
	public IPassiveResource getPassiveResource() {
		return passiveResource;
	}
	
	/**
	 * Register event handler to react on specific simulation events.
	 */
	private void registerEventHandler() {
		ISimulationMiddleware middleware = getSimulationMiddleware();
		
//		middleware.registerEventHandler(SimulationInitEvent.class, e -> init());
		middleware.registerEventHandler(SimulationStopEvent.class, e -> finalise());
		
		// setup system call parameter handling
		middleware.registerEventHandler(SystemRequestSpawnEvent.class, new BeforeSystemCallParameterHandler(this));
		middleware.registerEventHandler(SystemRequestFinishedEvent.class, new AfterSystemCallParameterHandler());
	}

	private void setupMeasurements() {
		// initialize measurement facade
		measurementFacade = new MeasurementFacade<>(SystemMeasurementConfiguration.from(this),
				new BundleProbeLocator<>(Activator.getContext().getBundle()));

		MeasurementStorage measurementStorage = getMeasurementStorage();
		measurementStorage.addIdExtractor(Request.class, c -> Long.toString(((Request)c).getId()));
		measurementStorage.addNameExtractor(Request.class, c -> ((Request)c).getName());
		measurementStorage.addIdExtractor(ForkedRequest.class, c -> Long.toString(((ForkedRequest)c).getEntityId()));
		measurementStorage.addNameExtractor(ForkedRequest.class, c -> ((ForkedRequest)c).getName());
		measurementStorage.addIdExtractor(Entity.class, c -> ((Entity)c).getId());
		measurementStorage.addNameExtractor(Entity.class, c -> ((Entity)c).getEntityName());
		
		// response time of external calls
		execute(new FindAllActionsByType<>(ExternalCallAction.class)).forEach(
				call -> measurementFacade.createCalculator(new ResponseTimeOfExternalCallsCalculator())
						.from(call.getAction(), "before", call.getAssemblyContext())
						.to(call.getAction(), "after", call.getAssemblyContext())
						.forEachMeasurement(m -> measurementStorage.putPair(m)));
		
//		// calculation time of internal actions [just as a proof of concept] 
//		execute(new FindAllActionsByType<>(InternalAction.class)).forEach(
//				action -> measurementFacade.createCalculator(new TimeSpanBetweenAbstractActionsCalculator())
//						.from(action.getAction(), "before", action.getAssemblyContext())
//						.to(action.getAction(), "after", action.getAssemblyContext())
//						.forEachMeasurement(m -> getSimulationMiddleware().getMeasurementStore().putPair(m)));
		
		// calculation time between two specific internal actions (one inside a fork) [just as a proof of concept]
//		Optional<ActionContext<InternalAction>> a1 = execute(new FindAllActionsByType<>(InternalAction.class)).stream()
//				.filter(ctx -> ctx.getAction().getId().equals("_0eyYUCHjEd6ZSMvOJK-6LQ")).findFirst();
//		
//		AssemblyContext assCtx = a1.get().getAssemblyContext();
//		
//		Optional<ActionContext<InternalAction>> a2 = execute(new FindAllActionsByType<>(InternalAction.class)).stream()
//				.filter(ctx -> ctx.getAction().getId().equals("_SljAsG0GEeWoctSuxfGXbw")).findFirst();
//		
//		measurementFacade.createCalculator(new TimeSpanBetweenAbstractActionsCalculator())
//				.from(a1.get().getAction(), "before", assCtx).to(a2.get().getAction(), "after", assCtx)
//				.forEachMeasurement(m -> System.out.println("-> " + m));
	}

	public SeffBehaviourInterpreter getSeffInterpreter() {
		return seffInterpreter;
	}

	/**
	 * Returns the resource environment comprising
	 * {@link SimulatedResourceContainer}.
	 * 
	 * @return the resource environment
	 */
	public SimulatedResourceEnvironment getResourceEnvironment() {
		return this.resourceEnvironment;
	}

	/**
	 * Returns the allocation of {@link AssemblyContext}s to
	 * {@link SimulatedResourceContainer}s.
	 * 
	 * @return a registry containing the resource allocations
	 */
	public AllocationRegistry getResourceAllocation() {
		return this.resourceAllocation;
	}

	/**
	 * Returns the component instance that is encapsulated by the specified
	 * assembly context.
	 * 
	 * @param assemblyContext
	 *            the assembly context
	 * @return the queried component instance
	 */
	public ComponentInstance getComponent(final AssemblyContext assemblyContext) {
		return this.componentRegistry.get(assemblyContext.getId());
	}
	
	public MeasurementFacade<SystemMeasurementConfiguration> getMeasurementFacade() {
		return measurementFacade;
	}

}
