package www.ontologyutils.toolbox;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SetUtils {

	// Prevent instantiation
	private SetUtils() {
	}
	
	public static <T> Set<Set<T>> powerSet(Set<T> set) {
		T[] element = (T[]) set.toArray();
		final int SET_LENGTH = 1 << element.length;
		Set<Set<T>> powerSet = new HashSet<>();
		for (int binarySet = 0; binarySet < SET_LENGTH; binarySet++) {
			Set<T> subset = new HashSet<>();
			for (int bit = 0; bit < element.length; bit++) {
				int mask = 1 << bit;
				if ((binarySet & mask) != 0) {
					subset.add(element[bit]);
				}
			}
			powerSet.add(subset);
		}
		return powerSet;
	}

	public static <T> T getRandom(Set<T> set) {
		int randomPick = ThreadLocalRandom.current().nextInt(0, set.size());
		return (T) (set.toArray())[randomPick];
	}
	
}
