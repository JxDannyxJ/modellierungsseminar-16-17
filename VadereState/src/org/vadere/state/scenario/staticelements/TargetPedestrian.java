package org.vadere.state.scenario.staticelements;

import org.vadere.state.attributes.scenario.AttributesTarget;
import org.vadere.state.scenario.DynamicElementRemoveListener;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.util.geometry.shapes.VShape;

/**
 * In the simulation a target also can be pedestrian. Thus this pedestrian may
 * obtain the characteristics of a target object.
 * //TODO: This has to be improved in the very future to grant the ability of being a target for every agent
 */
public class TargetPedestrian extends Target implements DynamicElementRemoveListener<Pedestrian> {

	private final Pedestrian pedestrian;
	private boolean isDeleted;

	/**
	 * Class constructor creating a new target specified as a pedestrian
	 * @param pedestrian the pedestrian which will get the properties of a target
	 */
	public TargetPedestrian(Pedestrian pedestrian) {
		super(new AttributesTarget(pedestrian));
		this.pedestrian = pedestrian;
		this.isDeleted = false;
	}

	@Override
	public VShape getShape() {
		return pedestrian.getShape();
	}

	@Override
	public boolean isTargetPedestrian() {
		return true;
	}

	public Pedestrian getPedestrian() {
		return this.pedestrian;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	@Override
	public void elementRemoved(Pedestrian pedestrian) {
		isDeleted = true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;

		TargetPedestrian that = (TargetPedestrian) o;

		if (isDeleted != that.isDeleted)
			return false;
		if (!pedestrian.equals(that.pedestrian))
			return false;

		return true;
	}
}
