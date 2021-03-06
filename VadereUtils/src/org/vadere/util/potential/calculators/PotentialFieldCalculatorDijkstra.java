package org.vadere.util.potential.calculators;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.vadere.util.math.MathUtil;
import org.vadere.util.potential.CellGrid;
import org.vadere.util.potential.CellState;
import org.vadere.util.potential.PathFindingTag;

public class PotentialFieldCalculatorDijkstra implements EikonalSolver {

	private CellGrid potentialField;
	private LinkedList<Point> targetPoints;

	PotentialFieldCalculatorDijkstra(CellGrid potentialField, LinkedList<Point> targetPoints) {
		this.potentialField = potentialField;
		this.targetPoints = targetPoints;
	}

	@Override
	public void initialize() {

		ComparatorPotentialFieldValue comparator = new ComparatorPotentialFieldValue(
				potentialField);
		PriorityQueue<Point> priorityQueue = new PriorityQueue<Point>(50,
				comparator);

		priorityQueue.addAll(targetPoints);

		Point currentPoint;
		List<Point> neighbors;
		double value;

		while (!priorityQueue.isEmpty()) {
			currentPoint = priorityQueue.remove();

			if (potentialField.getValue(currentPoint).tag != PathFindingTag.Target) {
				potentialField.getValue(currentPoint).tag = PathFindingTag.Reachable;
			}

			neighbors = MathUtil.getMooreNeighborhood(currentPoint);

			for (Point neighbor : neighbors) {
				PathFindingTag neighborTag = potentialField.getValue(neighbor).tag;

				if (neighborTag == PathFindingTag.Reachable) {
					value = potentialField.getValue(currentPoint).potential
							+ potentialField.pointDistance(currentPoint,
									neighbor);

					if (value < potentialField.getValue(neighbor).potential) {
						priorityQueue.remove(neighbor);
						potentialField.getValue(neighbor).potential = value;
						priorityQueue.add(neighbor);
					}
				} else if (neighborTag == PathFindingTag.Undefined) {
					value = potentialField.getValue(currentPoint).potential
							+ potentialField.pointDistance(currentPoint,
									neighbor);
					priorityQueue.add(neighbor);

					potentialField.setValue(neighbor, new CellState(value,
							PathFindingTag.Reachable));
				}
			}
		}

		System.out.println("Dijkstra is done.");
	}

	@Override
	public void update() {}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public CellGrid getPotentialField() {
		return potentialField;
	}

}
