package edu.kit.ipd.sdq.eventsim.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.AbstractEventSimModel;
import edu.kit.ipd.sdq.eventsim.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationStopEvent;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.PassiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.Instrumentor;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.InstrumentorBuilder;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimPassiveResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimulatedProcess;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

@Singleton
public class EventSimPassiveResourceModel extends AbstractEventSimModel implements IPassiveResource {

    // maps (AssemblyContext ID, PassiveResource ID) -> SimPassiveResource
    private Map<String, SimPassiveResource> contextToResourceMap;
    private WeakHashMap<IRequest, SimulatedProcess> requestToSimulatedProcessMap;
    
	private Instrumentor<SimPassiveResource, ?> instrumentor;
    
    @Inject
	public EventSimPassiveResourceModel(ISimulationMiddleware middleware, MeasurementStorage measurementStorage) {
		super(middleware, measurementStorage);
		contextToResourceMap = new HashMap<String, SimPassiveResource>();
		requestToSimulatedProcessMap = new WeakHashMap<>();
		init();
	}

	public void init() {
		// create instrumentor for instrumentation description
		SimulationConfiguration config = (SimulationConfiguration) getSimulationMiddleware().getSimulationConfiguration();
		instrumentor = InstrumentorBuilder
				.buildFor(config.getPCMModel())
				.inBundle(Activator.getContext().getBundle())
				.withDescription(config.getInstrumentationDescription())
				.withStorage(getMeasurementStorage())
				.forModelType(PassiveResourceRep.class)
				.withMapping((SimPassiveResource r) -> new PassiveResourceRep(r.getSpecification(), r.getAssemblyContext()))
				.createFor(new ResourceProbeConfiguration());
		
		MeasurementStorage measurementStorage = getMeasurementStorage();
		measurementStorage.addIdExtractor(SimPassiveResource.class, c -> ((SimPassiveResource)c).getSpecification().getId());
		measurementStorage.addNameExtractor(SimPassiveResource.class, c -> ((SimPassiveResource)c).getName());
		
		registerEventHandler();
	}
	
	private void registerEventHandler() {
		ISimulationMiddleware middleware = getSimulationMiddleware();
		
//		middleware.registerEventHandler(SimulationInitEvent.class, e -> init());
		middleware.registerEventHandler(SimulationStopEvent.class, e -> finalise());
	}

	public boolean acquire(IRequest request, AssemblyContext assCtx, PassiveResource specification, int num) {
        SimPassiveResource res = this.getPassiveResource(specification, assCtx);
        SimulatedProcess process = getOrCreateSimulatedProcess(request);
        boolean acquired = res.acquire(process, num, false, -1);

		return acquired;
	}

	/* (non-Javadoc)
	 * @see edu.kit.ipd.sdq.eventsim.AbstractEventSimModel#finalise()
	 */
	@Override
	public void finalise() {
		super.finalise();
		
		contextToResourceMap.clear();
	}

	public void release(IRequest request, AssemblyContext assCtx, PassiveResource specification, int i) {
        final SimPassiveResource res = this.getPassiveResource(specification, assCtx);
        res.release(getOrCreateSimulatedProcess(request), 1);
	}

    /**
     * @param specification
     *            the passive resource specification
     * @return the resource instance for the given resource specification
     */
    public SimPassiveResource getPassiveResource(final PassiveResource specification, AssemblyContext assCtx) {
        final SimPassiveResource simResource = findOrCreateResource(specification, assCtx);
        if (simResource == null) {
            throw new RuntimeException("Passive resource " + PCMEntityHelper.toString(specification)
                    + " for assembly context " + PCMEntityHelper.toString(assCtx) + " could not be found.");
        }
        return simResource;
    }
    
    
    /**
     * Finds the resource that has been registered for the specified type. If no resource of the
     * specified type can be found, the search continues with the parent resource container.
     * 
     * @param type
     *            the resource type
     * @return the resource of the specified type, if there is one; null else
     */
    public SimPassiveResource findOrCreateResource(PassiveResource specification, AssemblyContext assCtx) {
        if (!contextToResourceMap.containsKey(compoundKey(assCtx, specification))) {
            // create passive resource
            SimPassiveResource resource = ResourceFactory.createPassiveResource(this, specification, assCtx);

            // register the created passive resource
            contextToResourceMap.put(compoundKey(assCtx, specification), resource);
            
            // create probes and calculators (if requested by instrumentation description)
            instrumentor.instrument(resource);
        }
        return contextToResourceMap.get(compoundKey(assCtx, specification));
    }
    

    
    /**
     * Returns the simulated process that is used to schedule resource requests issued by this
     * Request on an active or passive resource.
     * 
     * @return the simulated process
     */
//    public SimulatedProcess getOrCreateSimulatedProcess(IRequest request) {
//        if (!requestToSimulatedProcessMap.containsKey(request)) {
//        	SimulatedProcess p = new SimulatedProcess(this, request, Long.toString(request.getId()));
//        	requestToSimulatedProcessMap.put(request, p);
//        }
//        return requestToSimulatedProcessMap.get(request);
//    }
    
    
    // TODO duplicate from active resource model
	public SimulatedProcess getOrCreateSimulatedProcess(IRequest request) {
		if (!requestToSimulatedProcessMap.containsKey(request)) {
			SimulatedProcess parent = null;
			if(request.getParent() != null) {
				parent = getOrCreateSimulatedProcess(request.getParent());
			}
			SimulatedProcess process = new SimulatedProcess(this, parent, request);

//			// add listener for request finish
//			EventSimEntity requestEntity = (EventSimEntity) request;
//			requestEntity.addEntityListener(new RequestFinishedHandler(process));

			requestToSimulatedProcessMap.put(request, process);
		}
		return requestToSimulatedProcessMap.get(request);
	}
    
	private String compoundKey(AssemblyContext specification,
			PassiveResource resource) {
		// TODO better use resource name "CPU", HDD, ... as second component!?
		return specification.getId() + resource.getId();
	}
    
}
