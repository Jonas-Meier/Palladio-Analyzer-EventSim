package edu.kit.ipd.sdq.eventsim.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.seff.AbstractAction;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.interpreter.state.AbstractInterpreterState;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModule;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModuleRegistry;
import edu.kit.ipd.sdq.eventsim.modules.SimulationStrategy;

/**
 * 
 * @author Philipp Merkle
 * 
 * @param <A>
 *            the least common parent type of all actions that are to be traversed
 */
@Singleton
public class TraversalStrategyRegistry<A extends Entity> {

    private static final Logger logger = Logger.getLogger(TraversalStrategyRegistry.class);

    private final Map<Class<? extends A>, ITraversalStrategy> handlerMap = new HashMap<>();

    @Inject
    public TraversalStrategyRegistry(Injector injector, SimulationModuleRegistry moduleRegistry) {
        for (SimulationModule m : moduleRegistry.getModules()) {
            // skip, if module is disabled
            if (!m.isEnabled()) {
                continue;
            }
            for (SimulationStrategy s : m.getSimulationStrategies()) {
                try {
                    Class<? extends A> actionType = (Class<? extends A>) Class.forName(s.getActionType());
                    ITraversalStrategy strategy = (ITraversalStrategy) s.getStrategy();
                    registerActionHandler(actionType, strategy);
                    injector.injectMembers(strategy);
                } catch (ClassNotFoundException e) {
                    logger.error(e);
                } catch (InvalidRegistryObjectException e) {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * Adds a handler for the specified action class. If a handler for the specified action class
     * already exists, the existing handler will be decorated by the specified handler.
     * 
     * @param actionClass
     *            the action class
     * @param handler
     *            the handler that is to be registered
     */
    public void registerActionHandler(final Class<? extends A> actionClass, final ITraversalStrategy handler) {
        // TODO
        // assert (UsagemodelPackage.eINSTANCE.getAbstractUserAction().isSuperTypeOf(
        // actionClass)) : "The parameter \"action\" has to be a subtype of AbstractUserAction, but
        // was "
        // + actionClass.getName();
        // if (handlerMap.containsKey(actionClass)) {
        // if (logger.isEnabledFor(Level.WARN))
        // logger.warn("Registered a handler for " + actionClass.getName()
        // + ", for which a handler was already registered. The former handler has been
        // overwritten.");
        // }
        if (handlerMap.containsKey(actionClass)) {
            handler.decorate(handlerMap.get(actionClass));
        }
        handlerMap.put(actionClass, handler);
    }

    /**
     * Removes the handler for the specified action class.
     * 
     * @param actionClass
     *            the action class whose handler is to be unregistered
     */
    public void unregisterActionHandler(final Class<? extends AbstractAction> actionClass) {
        // TODO
        // assert (UsagemodelPackage.eINSTANCE.getAbstractUserAction().isSuperTypeOf(
        // actionClass)) : "The parameter \"action\" has to be a subtype of AbstractUserAction, but
        // was "
        // + actionClass.getName();
        // if (handlerMap.containsKey(actionClass)) {
        // if (logger.isEnabledFor(Level.WARN))
        // logger.warn("Tried to unregister the action handler of " + actionClass.getName()
        // + ", but no handler has been registered for this action.");
        // }
        handlerMap.remove(actionClass);
    }

    public ITraversalStrategy<A, ? extends A, ?, ? extends AbstractInterpreterState<A>> lookup(
            Class<? extends A> type) {
        return handlerMap.get(type);
    }

}
