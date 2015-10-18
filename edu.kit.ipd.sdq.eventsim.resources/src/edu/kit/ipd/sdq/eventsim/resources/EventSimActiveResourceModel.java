package edu.kit.ipd.sdq.eventsim.resources;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

import de.uka.ipd.sdq.scheduler.ISchedulingFactory;
import de.uka.ipd.sdq.scheduler.factory.SchedulingFactory;
import de.uka.ipd.sdq.scheduler.resources.active.AbstractActiveResource;
import edu.kit.ipd.sdq.eventsim.AbstractEventSimModel;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.entities.IEntityListener;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.SimulationModel;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimActiveResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimulatedProcess;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

public class EventSimActiveResourceModel extends AbstractEventSimModel {

	private static final Logger logger = Logger.getLogger(EventSimActiveResourceModel.class);

	private ISchedulingFactory schedulingFactory;

	// maps (ResourceContainer ID, ResourceType ID) -> SimActiveResource
	private Map<String, SimActiveResource> containerToResourceMap;
	
	// TODO extract class
	private Map<IRequest, SimulatedProcess> requestToSimulatedProcessMap;

	private MeasurementFacade<ResourceProbeConfiguration> measurementFacade;
	
	public EventSimActiveResourceModel(ISimulationMiddleware middleware) {
		super(middleware);

		containerToResourceMap = new HashMap<String, SimActiveResource>();
		requestToSimulatedProcessMap = new WeakHashMap<IRequest, SimulatedProcess>();
	}

	@Override
	public void init() {
		// set up the resource scheduler
		SimulationModel simModel = (SimulationModel) this.getSimulationMiddleware().getSimulationModel();
		this.schedulingFactory = new SchedulingFactory(simModel);

		measurementFacade = new MeasurementFacade<>(new ResourceProbeConfiguration(), Activator.getContext()
				.getBundle());
		
		RMeasurementStore rstore = getSimulationMiddleware().getMeasurementStore();
		rstore.addIdExtractor(SimActiveResource.class, c -> ((SimActiveResource)c).getSpecification().getId());
		rstore.addIdExtractor(SimulatedProcess.class, c -> Long.toString(((SimulatedProcess)c).getEntityId()));
	}
	
//	private void initProbeSpecification() {
//		ProbeSpecContext probeContext = this.getSimulationMiddleware().getProbeSpecContext();
//		IProbeStrategyRegistry strategyRegistry = probeContext.getProbeStrategyRegistry();
//
//		/* RESOURCE_DEMAND */
//		// active resources
//		strategyRegistry.registerProbeStrategy(new TakeScheduledResourceDemandStrategy(), ProbeType.RESOURCE_DEMAND, SimActiveResource.class);
//
//		/* RESOURCE_STATE */
//		// active resources
//		strategyRegistry.registerProbeStrategy(new TakeScheduledResourceStateStrategy(), ProbeType.RESOURCE_STATE, SimActiveResource.class);
//
//	}

	public void consume(IRequest request, ResourceContainer resourceContainer, ResourceType resourceType, double absoluteDemand) {

		final SimActiveResource resource = findOrCreateResource(resourceContainer, resourceType);
		if (resource == null) {
			throw new RuntimeException("Could not find a resource of type " + resourceType.getEntityName());
		}

		resource.consumeResource(getOrCreateSimulatedProcess(request), absoluteDemand);
	}

	@Override
	public void finalise() {
		super.finalise();

		// clean up created resources
		for(Iterator<Map.Entry<String, SimActiveResource>> it = containerToResourceMap.entrySet().iterator(); it.hasNext();) {
		      Map.Entry<String, SimActiveResource> entry = it.next();
		      entry.getValue().deactivateResource();
		      it.remove();
		}

		measurementFacade = null;
		
		AbstractActiveResource.cleanProcesses();
	}

	public ISchedulingFactory getSchedulingFactory() {
		return schedulingFactory;
	}

	/**
	 * Registers a resource for the specified resource type. Only one resource
	 * can be registered for each resource type. Thus, providing a resource for
	 * an already registered resource type overwrites the existing resource.
	 * 
	 * @param resource
	 *            the resource that is to be registered
	 * @param type
	 *            the type of the resource
	 */
	private void registerResource(ResourceContainer specification, SimActiveResource resource, ResourceType type) {
		if (logger.isDebugEnabled()) {
			logger.debug("Registering a " + type.getEntityName() + " resource at " + PCMEntityHelper.toString(specification));
		}
		if (this.containerToResourceMap.containsKey(type)) {
			if (logger.isEnabledFor(Level.WARN))
				logger.warn("Registered a resource of type " + type.getEntityName() + ", but there was already a resource of this type. The existing resource has been overwritten.");
		}

		this.containerToResourceMap.put(compoundKey(specification, type), resource);
		
		// create corresponding probe
		measurementFacade.createProbe(resource, "queue_length").forEachMeasurement(
				m -> getSimulationMiddleware().getMeasurementStore().put(m));
		measurementFacade.createProbe(resource, "resource_demand").forEachMeasurement(
				m -> getSimulationMiddleware().getMeasurementStore().put(m));

		// initialise probe spec
//		this.execute(new BuildActiveResourceCalculators(this, resource));
//		this.execute(new MountActiveResourceProbes(this, resource));
	}

	/**
	 * Finds the resource that has been registered for the specified type. If no
	 * resource of the specified type can be found, the search continues with
	 * the parent resource container.
	 * 
	 * @param type
	 *            the resource type
	 * @return the resource of the specified type, if there is one; null else
	 */
	public SimActiveResource findOrCreateResource(ResourceContainer specification, ResourceType resourceType) {
		if (!containerToResourceMap.containsKey(compoundKey(specification, resourceType))) {
			// if (parent != null) {
			// return parent.findResource(type);
			// } else {
			// return null;
			// }
			// TODO create resource
			// create resource
			// ResourceType resourceType =
			// s.getActiveResourceType_ActiveResourceSpecification();

			ProcessingResourceSpecification s = null;
			for (ProcessingResourceSpecification spec : specification.getActiveResourceSpecifications_ResourceContainer()) {
				// TODO does this work!??
				if (spec.getActiveResourceType_ActiveResourceSpecification().equals(resourceType)) {
					s = spec;
					break;
				}
			}
			if (s == null) {
				// remove
				// TODO (simcomp) is also thrown if HDD is missing!
				throw new RuntimeException("refactoring went wrong :(");
			}

			SimActiveResource resource = ResourceFactory.createActiveResource(this, schedulingFactory, s);
			resource.setDescription(specification.getEntityName() + " [" + resourceType.getEntityName() + "] <" + specification.getId() + ">");

			// register the created resource
			registerResource(specification, resource, resourceType);
		}
		return containerToResourceMap.get(compoundKey(specification, resourceType));
	}

	private String compoundKey(ResourceContainer specification, ResourceType resourceType) {
		// TODO better use resource name "CPU", HDD, ... as second component!?
		return specification.getId() + resourceType.getId();
	}

	/**
	 * This handler reacts when the Request has been finished and informs the
	 * simulated process about that.
	 * 
	 * @author Philipp Merkle
	 */
	private class RequestFinishedHandler implements IEntityListener {

		private WeakReference<SimulatedProcess> process;

		public RequestFinishedHandler(SimulatedProcess process) {
			this.process = new WeakReference<SimulatedProcess>(process);
		}

		@Override
		public void enteredSystem() {
			// nothing to do
		}

		@Override
		public void leftSystem() {
			process.get().terminate();
			requestToSimulatedProcessMap.remove(process.get().getRequest());
		}

	}

	/**
	 * Returns the simulated process that is used to schedule resource requests
	 * issued by this Request on an active or passive resource.
	 * 
	 * @return the simulated process
	 */
	public SimulatedProcess getOrCreateSimulatedProcess(IRequest request) {
		if (!requestToSimulatedProcessMap.containsKey(request)) {
			SimulatedProcess parent = null;
			if(request.getParent() != null) {
				parent = getOrCreateSimulatedProcess(request.getParent());
			}
			SimulatedProcess process = new SimulatedProcess(this, parent, request);

			// add listener for request finish
			EventSimEntity requestEntity = (EventSimEntity) request;
			requestEntity.addEntityListener(new RequestFinishedHandler(process));

			requestToSimulatedProcessMap.put(request, process);
		}
		return requestToSimulatedProcessMap.get(request);
	}
	
	
//	public SimulatedProcess getOrCreateSimulatedProcess(IRequest request) {
//		if (!requestToSimulatedProcessMap.containsKey(request)) {
//			SimulatedProcess process = new SimulatedProcess(this, request, Long.toString(request.getId()));
//
//			// add listener for request finish
//			EventSimEntity requestEntity = (EventSimEntity) request;
//			requestEntity.addEntityListener(new RequestFinishedHandler(process));
//
//			requestToSimulatedProcessMap.put(request, process);
//		}
//		return requestToSimulatedProcessMap.get(request);
//	}
	

}
