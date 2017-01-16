package org.vadere.state.attributes.models;

import java.util.Arrays;
import java.util.List;

import org.vadere.state.attributes.Attributes;

public class AttributesCGM extends Attributes {
	private double groupMemberRepulsionFactor = 0.01;
	private double leaderAttractionFactor = 0.003;
	private List<Double> groupSizeDistribution = Arrays.asList(0.0, 0.0, 1.0);

	public AttributesCGM() {
	}

	public double getGroupMemberRepulsionFactor() {
		return groupMemberRepulsionFactor;
	}

	public double getLeaderAttractionFactor() {
		return leaderAttractionFactor;
	}

	public List<Double> getGroupSizeDistribution() {
		return groupSizeDistribution;
	}
}
