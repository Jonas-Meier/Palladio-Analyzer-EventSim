package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.SetVariableAction;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe;
import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingTraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseNextAction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;
import edu.kit.ipd.sdq.eventsim.util.ParameterHelper;

/**
 * This traversal strategy is responsible for {@link SetParameterAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class SetVariableActionTraversalStrategy
        extends DecoratingTraversalStrategy<AbstractAction, SetVariableAction, Request, RequestState> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(SetVariableAction action, Request entity,
            RequestState state) {
        traverseDecorated(action, entity, state);
        StackContext ctx = state.getStoExContext();
        SimulatedStackframe<Object> currentStackFrame = ctx.getStack().currentStackFrame();

        ParameterHelper.evaluateParametersAndCopyToFrame(action.getLocalVariableUsages_SetVariableAction(),
                currentStackFrame, currentStackFrame);

        return new TraverseNextAction<>(action.getSuccessor_AbstractAction());
    }

}
