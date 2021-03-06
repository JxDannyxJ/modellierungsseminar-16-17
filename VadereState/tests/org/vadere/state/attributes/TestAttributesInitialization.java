package org.vadere.state.attributes;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;
import org.vadere.state.attributes.scenario.AttributesAgent;

import com.google.gson.Gson;

public class TestAttributesInitialization {

	private static final double delta = 1e-8;
	private AttributesAgent attributesPedestrian;
	private String store;

	/**
	 * Creates a key/value store.
	 */
	@Before
	public void setUp() {

		store = "{speedDistributionMean:0.2}";
	}

	/**
	 * Test method for
	 * {@link org.vadere.state.attributes.models.AttributesODEIntegrator#AttributesODEModel(java.util.Map)}
	 * . Asserts the attributes are initialized correctly.
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testInit() throws IllegalArgumentException, IllegalAccessException {
		// correct case
		attributesPedestrian = new Gson().fromJson(store, AttributesAgent.class);

		assertArrayEquals(new double[] {0.2}, new double[] {attributesPedestrian.getSpeedDistributionMean()}, delta);

	}

	/**
	 * Test method for
	 * {@link org.vadere.state.attributes.models.AttributesODEIntegrator#AttributesODEModel(java.util.Map)}
	 * . Asserts the default attributes are initialized correctly.
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testInitDefault() throws IllegalArgumentException, IllegalAccessException {
		// correct case
		store = "{}";
		attributesPedestrian = new Gson().fromJson(store, AttributesAgent.class);

		assertArrayEquals(new double[] {1.34}, new double[] {attributesPedestrian.getSpeedDistributionMean()},
				delta);

	}

}
