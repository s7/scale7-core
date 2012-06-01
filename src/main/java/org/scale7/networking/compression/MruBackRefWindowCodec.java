package org.scale7.networking.compression;

import java.util.Map;

import org.scale7.collections.LowMemoryHashMap;


/**
 * Encodes and decodes back references to values. A window of values is maintained, and
 * references to the least recently values are recycled once the window size is exceeded.
 * Using this class, a stream through which serialized object values must be passed can
 * be compressed by passing back references to values that are inside the current window.
 * This class optimizes for high throughput streams by minimizing the GC pressure it
 * generates.
 * 
 * @author dominicwilliams
 *
 * @param <T>	The type of value to which back references are generated
 */
public class MruBackRefWindowCodec<T> {
	final Map<T, Integer> valToRef;
	int newRef=-1, headRef=-1, tailRef=-1;
	final int window;
	final Object[] values;
	final int[] prevRef;
	final int[] nextRef;
	
	/**
	 * Constructor. Both the encoder and decoder must be constructed with same window size.
	 * @param window	The window size for which back references are maintained
	 * @param encoder	Whether this codec will perform encoding (requires more memory)
	 */
	public MruBackRefWindowCodec(int window, boolean encoder) {
		if (window < 1)
			throw new RuntimeException("Minimum window size is 1");
		valToRef = encoder ? new LowMemoryHashMap<T, Integer>() : null;
		this.window = window;
		values = new Object[window];
		prevRef = new int[window];
		nextRef = new int[window];
	}
	
	/**
	 * To be used by the stream creator to encode a value either as a reference or the value
	 * itself. If -1 is returned then the stream should send the value itself, otherwise the
	 * stream should send the back reference (which the receiver can use to retrieve the value).
	 * @param value		The object value to be passed across the stream
	 * @return			A back reference to the value
	 */
	public int encodeRefOrValue(T value) {
		Integer ref = valToRef.get(value);
		if (ref != null) {
			touchRef(ref);
			return ref;
		} else {
			touchValue(value);
			return -1;
		}
	}
	
	/**
	 * To be used by the stream receiver to record that a value has been read and update the 
	 * window state accordingly.
	 * @param value		The object value that has been read.
	 */
	public void touchValue(T value) {
		int nextRef;
		if ((newRef+1) < window) {
			nextRef = ++newRef;
		} else {
			nextRef = tailRef;
			if (valToRef != null)
				valToRef.remove(values[tailRef]);
		}
		values[nextRef] = value;
		if (valToRef != null)
			valToRef.put(value, nextRef);
		touchRef(nextRef);
	}
	
	/**
	 * To be used by the stream receiver to record that a reference has been read and update
	 * the window state accordingly. This also resolves the reference into a value.
	 * @param ref		The reference that has been received.
	 * @return			The object value the reference refers to.
	 */
	@SuppressWarnings("unchecked")
	public T touchRef(int ref) {
		if (headRef == ref) {
			;
		} else if (headRef == -1) {
			headRef = tailRef = ref;
			nextRef[ref] = prevRef[ref] = -1;
		} else {
			prevRef[headRef] = ref;
			nextRef[ref] = headRef;
			if (ref == tailRef && prevRef[ref] != -1) {
				tailRef = prevRef[ref];
				nextRef[tailRef] = -1;
			}
			headRef = ref;
			prevRef[ref] = -1;
		}
		return (T) values[ref];
	}
}