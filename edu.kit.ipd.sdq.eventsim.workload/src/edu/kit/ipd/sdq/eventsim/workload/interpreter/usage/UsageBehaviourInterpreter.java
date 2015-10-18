package edu.kit.ipd.sdq.eventsim.workload.interpreter.usage;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import edu.kit.ipd.sdq.eventsim.AbstractEventSimModel;
import edu.kit.ipd.sdq.eventsim.core.palladio.state.StateExchange;
import edu.kit.ipd.sdq.eventsim.core.palladio.state.UserState;
import edu.kit.ipd.sdq.eventsim.interpreter.BehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalStrategy;
import edu.kit.ipd.sdq.eventsim.workload.command.usage.FindActionInUsageBehaviour;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.listener.IUsageTraversalListener;

/**
 * An interpreter for {@link UsageScenario}s.
 * 
 * @author Philipp Merkle
 * 
 * @see BehaviourInterpreter
 */
public class UsageBehaviourInterpreter extends BehaviourInterpreter<AbstractUserAction, User, UserState> {

	private UsageInterpreterConfiguration configuration;

	public UsageBehaviourInterpreter(UsageInterpreterConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beginTraversal(final User user, final ScenarioBehaviour behaviour) {
		user.notifyEnteredSystem();

		// initialise traversal state and StoEx context
		UserState state = new UserState();
		state.pushStackFrame();
		state.getStoExContext().getStack().createAndPushNewStackFrame();

		// store created state to the state exchange service
		StateExchange.storeUserState(user.getId(), state);

		// find start action
		final AbstractEventSimModel model = user.getEventSimModel();
		final Start start = model.execute(new FindActionInUsageBehaviour<Start>(behaviour, Start.class));

		// begin traversal
		state.setCurrentPosition(start);
		this.traverse(user, start, state);
	}

	/**
	 * {@inheritDoc}
	 */
	public void resumeTraversal(final User user, final UserState state) {
		// find next action and resume traversal
		super.traverse(user, state.getCurrentPosition(), state);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ITraversalStrategy<AbstractUserAction, ? extends AbstractUserAction, User, UserState> loadTraversalStrategy(final EClass eclass) {
		return this.configuration.getHandlerMap().get(eclass);
	}

	@Override
	public UsageInterpreterConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyAfterListener(final AbstractUserAction action, final User user, UserState state) {
		for (final IUsageTraversalListener l : this.configuration.getTraversalListenerList()) {
			l.after(action, user, state);
		}
		final List<IUsageTraversalListener> listeners = this.configuration.getTraversalListenerMap().get(action);
		if (listeners != null) {
			for (final IUsageTraversalListener l : listeners) {
				l.after(action, user, state);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyBeforeListener(final AbstractUserAction action, final User user, UserState state) {
		for (final IUsageTraversalListener l : this.configuration.getTraversalListenerList()) {
			l.before(action, user, state);
		}
		final List<IUsageTraversalListener> listeners = this.configuration.getTraversalListenerMap().get(action);
		if (listeners != null) {
			for (final IUsageTraversalListener l : listeners) {
				l.before(action, user, state);
			}
		}
	}

}
