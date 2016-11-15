package org.vadere.gui.topographycreator.control;

import org.vadere.state.attributes.Attributes;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.lang.reflect.Field;

/**
 * Setter and getter implementation to modify Attributes. This class uses use of reflection.
 * Do not use this class outside of the topographycreator package, or even not outside this
 * control-package!
 * 
 */
public class ReflectionAttributeModifier {
	/**
	 * Sets the shape to the attributes of an topography element. Use this method only in the
	 * control!
	 * 
	 * @param element the attributes
	 * @param shape the shape
	 */
	static void setShapeToAttributes(final ScenarioElement element, final VShape shape) {
		try {
			Field field;
			if (element instanceof Pedestrian) {
				double x = shape.getBounds2D().getCenterX();
				double y = shape.getBounds2D().getCenterY();
				((Pedestrian) element).setPosition(new VPoint(x, y));
			} else {
				Attributes attributes =
						org.vadere.gui.components.control.ReflectionAttributeModifier.getAttributes(element);
				field = attributes.getClass().getDeclaredField("shape");
				field.setAccessible(true);
				field.set(attributes, shape);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
