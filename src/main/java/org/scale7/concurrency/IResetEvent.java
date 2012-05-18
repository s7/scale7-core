package org.scale7.concurrency;

import java.util.concurrent.TimeUnit;

public interface IResetEvent {
	public void set();
	
	public void reset();
	
	public void waitOne();
	
	public boolean waitOne(int timeout, TimeUnit unit);
	
	public boolean isSignalled();
}
