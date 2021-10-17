/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.aldan3.model.Log;
import org.aldan3.model.ServiceProvider;

import com.beegman.webbee.model.AppModel;

public class Cron <T, A extends AppModel> implements ServiceProvider {
	// TODO add a task type
	public static final String NAME = "##cron";
	protected A appModel;
	ScheduledThreadPoolExecutor executor;
	ScheduledFuture<T> taskControl;
	
	public Cron(A m) {
		appModel = m;
		executor = initExecutor();
		schedule();
	}
	
	@Override
	public String getPreferredServiceName() {
		return getClass().getName();
	}

	@Override
	public Object getServiceProvider() {
		return this;
	}
	
	protected ScheduledThreadPoolExecutor initExecutor() {
		return new ScheduledThreadPoolExecutor(getPoolSize(), getThreadFactory());
	}
	
	protected int getPoolSize() {
		return 1;
	}
	
	protected ThreadFactory getThreadFactory() {
		return new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread result = new Thread(r, getName());
				result.setDaemon(true);
				result.setPriority(getPriority());
				return result;
			}
			
		};
	}
	
	protected int getPriority() {
		return Thread.MIN_PRIORITY;
	}
	
	protected boolean isPeriodic() {
		return true;
	}
	
	protected TimeUnit getTimeUnit() {
		return TimeUnit.HOURS;
	}
	
	protected long getInterval() {
		return 1;
	}
	
	protected long getInitialInterval() {
		return 0;
	}
	
	protected void schedule() {
		if (isPeriodic()) {
			taskControl = (ScheduledFuture<T>) executor.scheduleWithFixedDelay(getTask(), getInitialInterval(), getInterval(), getTimeUnit());
		} else {
			taskControl = (ScheduledFuture<T>) executor.schedule(getTask(), getInitialInterval(), getTimeUnit());
			try {
				taskControl.get();
			} catch (InterruptedException e) {
				Log.l.error(getName(), e);
			} catch (ExecutionException e) {
				Log.l.error(getName(), e);
			}
		}
	}
	
	protected String getName() {
		return NAME;
	}
	
	public void runNow() {
		executor.submit(getTask());
	}
	
	public boolean cancel() {
		return taskControl.cancel(false);
	}
	
	protected Runnable getTask() {
		return null;
	}
	
	public void suspend() {
		
	}
	
	public void resume() {
		
	}
	
	public void shutdown() {
		executor.shutdownNow();
	}
	
}
