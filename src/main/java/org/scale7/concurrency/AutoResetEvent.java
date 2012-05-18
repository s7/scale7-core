package org.scale7.concurrency;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A simple event which can be either signalled or non-signalled. Waiting for a
 * signalled event does not block. Waiting for a non-signalled event blocks until
 * the event becomes signalled or the thread is interrupted. When a thread waits
 * for the event and then returns, it automatically sets the event to a 
 * non-signalled state. Therefore, if the event starts in a non-signalled state,
 * the number of waiting threads it has allowed to pass is equal to the number
 * of times it has been signalled via set(). 
 * @author dominicwilliams
 *
 */
public class AutoResetEvent implements IResetEvent {
	private final Semaphore event;
	private final Integer mutex;
	
	public AutoResetEvent(boolean signalled) {
		event = new Semaphore(signalled ? 1 : 0);
		mutex = new Integer(-1);
		
	}
	
	/**
	 * Signal this event
	 */
	public void set() {
		synchronized (mutex) {
			if (event.availablePermits() == 0)
				event.release();	
		}
	}
	
	/**
	 * Set this event to the non-signalled state
	 */
	public void reset() {
		event.drainPermits();
	}
	
	/**
	 * Wait for this event to become signalled. If several threads are waiting
	 * only one will be allowed to pass each time that it is signalled. Waiting throws
	 * an exception if the thread is interrupted.
	 */
	public void waitOne() {
		try {
			event.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Wait for this event to become signalled. If several threads are waiting
	 * only one will be allowed to pass each time that it is signalled. Waiting throws an
	 * exception if the thread is interrupted.
	 */
	public boolean waitOne(int timeout, TimeUnit unit) {
		try {
			return event.tryAcquire(timeout, unit);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}	
	
	/**
	 * Is this event signalled.
	 */
	public boolean isSignalled() {
		return event.availablePermits() > 0;
	}	
}
