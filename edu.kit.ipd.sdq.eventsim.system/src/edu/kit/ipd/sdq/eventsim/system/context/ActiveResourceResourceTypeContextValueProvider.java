//package edu.kit.ipd.sdq.eventsim.system.context;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.eclipse.emf.common.util.EList;
//import org.palladiosimulator.pcm.resourcetype.ResourceType;
//
//import edu.kit.ipd.sdq.simcomp.component.IPCMModel;
//import edu.kit.ipd.sdq.simcomp.component.meta.IContextFieldValueProvider;
//
///**
// * Context field value provider for the resource type parameter of the active
// * resource simulation context.
// * 
// * @author Christoph Föhrdes
// * 
// */
//public class ActiveResourceResourceTypeContextValueProvider implements IContextFieldValueProvider {
//
//	private static final long serialVersionUID = 8606242044084081421L;
//
//	@Override
//	public List<String> getPossibleValues(IPCMModel model) {
//		List<String> values = new ArrayList<String>();
//
//		EList<ResourceType> resourceTypesRepo = model.getResourceRepository().getAvailableResourceTypes_ResourceRepository();
//		for (ResourceType resourceType : resourceTypesRepo) {
//			values.add(resourceType.getEntityName() + "#" + resourceType.getId());
//		}
//
//		return values;
//	}
//
//}
