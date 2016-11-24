package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.StartAction;

import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingTraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseNextAction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;

/**
 * This traversal strategy is responsible for {@link StartAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class StartActionTraversalStrategy
        extends DecoratingTraversalStrategy<AbstractAction, StartAction, Request, RequestState> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(final StartAction action, final Request request,
            final RequestState state) {
        traverseDecorated(action, request, state);
        return new TraverseNextAction<>(action.getSuccessor_AbstractAction());
    }

}
