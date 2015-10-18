package edu.kit.ipd.sdq.eventsim.system.interpreter.seff.strategies;

import java.util.List;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ForkAction;
import org.palladiosimulator.pcm.seff.ForkedBehaviour;

import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.InterruptTraversal;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystemModel;
import edu.kit.ipd.sdq.eventsim.system.entities.ForkedRequest;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.events.BeginForkedBehaviourTraversalEvent;
import edu.kit.ipd.sdq.eventsim.system.events.ResumeSeffTraversalEvent;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;

public class ForkActionTraversalStrategy implements ITraversalStrategy<AbstractAction, ForkAction, Request, RequestState> {

    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(ForkAction fork, Request request, RequestState state) {
    	EventSimSystemModel systemModel = (EventSimSystemModel) request.getEventSimModel();
        new ResumeSeffTraversalEvent(systemModel, state).schedule(request, 0);

        List<ForkedBehaviour> asynchronousBehaviours = fork.getAsynchronousForkedBehaviours_ForkAction();
        for (ForkedBehaviour b : asynchronousBehaviours) {
            ForkedRequest forkedRequest = new ForkedRequest(systemModel, b, true, request);
            
            // clone state because the state could be modified if the ResumeSeffTraversalEvent scheduled above is executed before the BeginForkedBehaviourTraversalEvent.  
            RequestState clonedState = null;
            try {
            	clonedState = state.clone();
            } catch (CloneNotSupportedException e) {
            	// this can not happen as long as there is a clone()-method
				throw new RuntimeException(e);
			}
            
			new BeginForkedBehaviourTraversalEvent(systemModel, b, clonedState).schedule(forkedRequest, 0);
        }

        if (fork.getSynchronisingBehaviours_ForkAction() != null) {
            throw new EventSimException("Synchronous forked behaviours are not yet supported.");
        }

        // TODO nur zul�ssig, falls keine synchronen Behaviours vorhanden. Ansonsten m�sste hier
        // gewartet werden, bis alle forked behaviours abgearbeitet sind.
        return new InterruptTraversal<>(fork.getSuccessor_AbstractAction());
        // return new TraverseNextAction<AbstractAction>(fork.getSuccessor_AbstractAction());
    }

}
