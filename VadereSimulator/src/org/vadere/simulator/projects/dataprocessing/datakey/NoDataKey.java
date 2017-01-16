package org.vadere.simulator.projects.dataprocessing.datakey;

/**
 * @author Mario Teixeira Parente
 */

public final class NoDataKey implements DataKey<NoDataKey> {

	private static NoDataKey key;

	private NoDataKey() {
	}

	@Override
	public int compareTo(final NoDataKey o) {
		return 0;
	}

	public static NoDataKey key() {
		if (key == null)
			key = new NoDataKey();

		return key;
	}
}
