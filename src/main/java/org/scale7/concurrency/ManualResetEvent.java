package org.scale7.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A simple event which can be either signalled or non-signalled. Waiting for a
 * signalled event does not block. Waiting for a non-signalled event blocks until
 * the event becomes signalled or the thread is interrupted. When a thread waits
 * for the event it does not affect the signalled status. This must be modified
 * manually.
 * @author dominicwilliams
 *
 */
public class ManualResetEvent implements IResetEvent {
	private volatile CountDownLatch event;
	private final Integer mutex;
	
	public ManualResetEvent(boolean signalled) {
		mutex = new Integer(-1);
		if (signalled) {
			event = new CountDownLatch(0);
		} else {
			event = new CountDownLatch(1);
		}
	}
	
	/**
	 * Make this event signalled
	 */
	public void set() {
		event.countDown();
	}
	
	/**
	 * Make this event non-signalled
	 */
	public void reset() {
		synchronized (mutex) {
			if (event.getCount() == 0) {
				event = new CountDownLatch(1);
			}
		}
	}
	
	/**
	 * Wait for this event to become signalled. Waiting throws an exception if
	 * the thread is interrupted.
	 */
	public void waitOne() {
		try {
			event.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Wait for this event to become signalled. Waitint throws an exception if the
	 * thread is interrupted. 
	 */
	public boolean waitOne(int timeout, TimeUnit unit) {
		try {
			return event.await(timeout, unit);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Determine if this event is currently signalled
	 */
	public boolean isSignalled() {
		return event.getCount() == 0;
	}
}
