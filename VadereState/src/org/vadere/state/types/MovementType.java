package org.vadere.state.types;

/**
 * Enumeration holding information of the movement type of the scenario
 * elements. Examples for NONE = Obstacle, Stairs, Source, Target, Teleporter
 * Examples for DIRECTIONAL = Train, Car, Horse, Trailer
 *
 */
public enum MovementType {
	ARBITRARY, DIRECTIONAL, NONE
}
