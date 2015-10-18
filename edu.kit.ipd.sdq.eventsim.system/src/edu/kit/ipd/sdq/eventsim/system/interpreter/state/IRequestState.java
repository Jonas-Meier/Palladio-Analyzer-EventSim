package edu.kit.ipd.sdq.eventsim.system.interpreter.state;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.interpreter.state.IInterpreterState;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.ComponentInstance;

/**
 * This interface specifies which state information can be set and returned for a {@link Request}
 * entity.
 * 
 * @author Philipp Merkle
 * 
 */
public interface IRequestState extends IInterpreterState<AbstractAction> {

    /**
     * Returns the component whose SEFF is being traversed currently.
     */
    public ComponentInstance getComponent();

    /**
     * Sets the component whose SEFF is being traversed currently.
     * 
     * @param component
     *            the component whose behaviour is under traversal
     */
    public void setComponent(ComponentInstance component);

}
