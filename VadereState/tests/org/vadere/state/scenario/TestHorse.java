package org.vadere.state.scenario;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.scenario.dynamicelements.Horse;

public class TestHorse {

	private Horse horse;

	@Before
	public void SetUp() {
		this.horse = createHorse();
		horse.setNextTargetListIndex(0);
	}

	@Test
	public void testSetNextTargetListIndex() {
		horse.setNextTargetListIndex(1);
		assertEquals(1, horse.getNextTargetListIndex());
	}

	@Test
	public void testIncrementNextTargetListIndex() {
		assertEquals(0, horse.getNextTargetListIndex());
		horse.incrementNextTargetListIndex();
		assertEquals(1, horse.getNextTargetListIndex());
	}

	@Test
	public void testGetNextTargetId() {
		horse.getTargets().add(3);
		assertEquals(3, horse.getNextTargetId());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetNextTargetIdFail() {
		horse.getTargets().clear();
		horse.getNextTargetId();
	}

	private Horse createHorse() {
		return new Horse(new AttributesHorse(), new Random(0));
	}
}
