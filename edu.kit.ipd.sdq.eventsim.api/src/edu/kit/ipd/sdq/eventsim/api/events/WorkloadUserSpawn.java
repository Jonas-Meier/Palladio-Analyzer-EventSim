package edu.kit.ipd.sdq.eventsim.api.events;

import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;

/**
 * Indicates that a new {@link IUser} has been created and waits to be simulated.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 * 
 */
public class WorkloadUserSpawn extends SimulationEvent {

	public static final String EVENT_ID = SimulationEvent.ID_PREFIX + "workload/USER_SPAWN";

	private IUser user;

	public WorkloadUserSpawn(IUser user) {
		super(EVENT_ID);
		this.user = user;
	}

	/**
	 * @return the newly created user, which is about to be simulated
	 */
	public IUser getUser() {
		return user;
	}

}
