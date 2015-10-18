package edu.kit.ipd.sdq.eventsim.workload.generator;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.ClosedWorkload;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import edu.kit.ipd.sdq.eventsim.api.events.WorkloadUserFinished;
import edu.kit.ipd.sdq.eventsim.entities.IEntityListener;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModel;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.events.BeginUsageTraversalEvent;

/**
 * A closed workload is a workload sustaining a fixed amount of {@link User}s, which are called the workload population.
 * The workload starts with generating a whole user generation. Whenever a user finishes its usage scenario, a new user
 * is generated after waiting a specified amount of time. This duration is called the think time of an user.
 * 
 * @author Philipp Merkle
 * 
 */
public class ClosedWorkloadGenerator implements IWorkloadGenerator {

	private final EventSimWorkloadModel model;
	private final ClosedWorkload workload;
	private final int population;
	private final PCMRandomVariable thinkTime;

	/**
	 * Constructs a closed workload in accordance with the specified workload description.
	 * 
	 * @param model
	 *            the model
	 * @param workload
	 *            the workload description
	 */
	public ClosedWorkloadGenerator(final EventSimWorkloadModel model, final ClosedWorkload workload) {
		this.model = model;
		this.workload = workload;
		this.population = workload.getPopulation();
		this.thinkTime = workload.getThinkTime_ClosedWorkload();

		ISimulationMiddleware middleware = model.getSimulationMiddleware();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processWorkload() {
		// spawn initial user population
		for (int i = 0; i < this.population; i++) {
			this.spawnUser();
		}
	}

	/**
	 * Creates a new user and schedules him to enter the system after the think time has passed.
	 */
	private void spawnUser() {
		// create the user
		final UsageScenario scenario = this.workload.getUsageScenario_Workload();
		final User user = new User(this.model, scenario);

		// when the user leaves the system, we schedule a new one
		user.addEntityListener(new IEntityListener() {

			@Override
			public void enteredSystem() {
				// nothing to do
			}

			@Override
			public void leftSystem() {
				// trigger event that the user finished his work
				model.getSimulationMiddleware().triggerEvent(new WorkloadUserFinished(user));

				ClosedWorkloadGenerator.this.spawnUser();
			}

		});
		final double waitingTime = StackContext.evaluateStatic(this.thinkTime.getSpecification(), Double.class);
		new BeginUsageTraversalEvent(this.model, scenario).schedule(user, waitingTime);
	}

}
