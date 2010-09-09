package org.scale7.collections;

import java.util.Arrays;

/**
 * Set of shorts backed by short array. The class is thread safe because a complete copy of the
 * array is made each time the set is modified. No synchronization is needed to query the array, and
 * this collection will be quick in cases where modifications are few, but reads occur very frequently.
 * This array is also much faster than a CopyOnWriteArraySet<Short> because it does not perform
 * boxing and unboxing of shorts, and also utilizes less memory since separate objects must not be
 * created for each entry.
 *
 * @author dominicwilliams
 *
 */
public class ShortCopyOnWriteArraySet {

	/**
	 * Constructs an empty set of shorts
	 */
	public ShortCopyOnWriteArraySet() {
	}

	/**
	 * Constructs a set of shorts
	 * @param values The initial values of the set
	 */
	public ShortCopyOnWriteArraySet(short[] values) {
		set(values);
	}

	/**
	 * Set the items inside the set
	 * @param values The new values to be contained in the set
	 */
	public void set(short[] values) {
		short[] newArray = Arrays.copyOf(values, values.length); // take defensive copy!
		Arrays.sort(newArray, 0, newArray.length);
		array = newArray;
	}

	/**
	 * Add new values to the set
	 * @param values The values to be added
	 * @return <code>true</code> if some values where not already present in the set, <code>false</code> otherwise
	 */
	public boolean add(short[] values) {
		synchronized (this) {
			if (array == null) {
				set(values);
				return true;
			} else {
				int newValueCount = 0;
				for (short value : values)
					if (!contains(value))
						newValueCount++;
				if (newValueCount == 0)
					return false;
				int newArrayLen = array.length + newValueCount;
				short[] newArray = Arrays.copyOf(array, newArrayLen);
				int i = array.length;
				for (short value : values)
					if (!contains(value))
						newArray[i++] = value;
				Arrays.sort(newArray, 0, newArray.length);
				array = newArray;
				return true;
			}
		}
	}

	/**
	 * Remove values from the set
	 * @param values The values to be removed
	 * @return <code>true</code> if some values where present in the set, <code>false</code>
	 */
	public boolean remove(short[] values) {
		synchronized (this) {
			if (array == null) {
				return false;
			} else {
				int removeCount = 0;
				for (short rem : values)
					if (contains(rem))
						removeCount++;
				if (removeCount == 0)
					return false;
				int newArrayLen = array.length - removeCount;
				short[] newArray = new short[newArrayLen];
				int copiedIdx = 0;
				for (int i=0; i<array.length; i++) {
					short existingValue = array[i];
					boolean keep = true;
					for (short rem : values)
						if (existingValue == rem) {
							keep = false;
							break;
						}
					if (keep)
						newArray[copiedIdx++] = existingValue;
				}
				array = newArray;
				return true;
			}
		}
	}

	/**
	 * Clear all entries from the set
	 */
	public void clear() {
		array = null;
	}

	/**
	 * Search the set to see if it contains a given value
	 * @param value The value to search for
	 * @return Whether the value was present in the set
	 */
	public boolean contains(short value) {
		if (array == null)
			return false;
		else
			return Arrays.binarySearch(array, 0, array.length, value) >= 0;
	}

	/**
	 * The number of entries in the set
	 * @return The size of the set
	 */
	public int size() {
		if (array == null)
			return 0;
		else
			return array.length;
	}

	/**
	 * The contents of the set as an array. A copy of the set's underlying array is made.
	 * @return The contents of the set as an array
	 */
	public short[] toArray() {
		return toArray(true);
	}

	/**
	 * The contents of the set as an array. Using this method the set's underlying array may be accessed. This
	 * may avoid an unnecessary array allocation in performance critical situations, however if the underlying
	 * array is modified, the set may become broken.
	 * @param copy Whether a copy of the set's underlying array should be returned
	 * @return The contents of the set as an array
	 */
	public short[] toArray(boolean copy) {
		if (array == null)
			return new short[0];
		else {
			if (copy) {
				short[] itemArray = array;
				return Arrays.copyOf(itemArray, itemArray.length);
			} else
				return array;
		}
	}

	volatile short[] array;
}
