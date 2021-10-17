/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import org.aldan3.model.ServiceProvider;

import com.beegman.webbee.model.UIEvent;

public class AsyncUpdater implements ServiceProvider, Runnable, AsyncListener {
	public static final String ATTR_UI_EVENTS = "##uievents##";

	public static final String NAME = "##AsyncUpdater##";

	public static final String SAVED_ID = "##SavedIdr##";

	private LinkedHashMap<String, LinkedList<AsyncContext>> requesters; // pile of async requests

	private LinkedBlockingQueue<UIEventHolder> uiQueue;

	private LinkedBlockingQueue<UIEventHolder> reQueue; // to reinsert unprocessed event for differed processing

	private boolean stopped;

	private Thread t;

	public AsyncUpdater() {
		requesters = new LinkedHashMap<String, LinkedList<AsyncContext>>();
		uiQueue = new LinkedBlockingQueue<UIEventHolder>();
		reQueue = new LinkedBlockingQueue<UIEventHolder>();
		t = new Thread(this, getPreferredServiceName());
		t.setDaemon(true);
		t.setPriority(Thread.NORM_PRIORITY);
		t.start();
	}

	@Override
	public String getPreferredServiceName() {
		return NAME;
	}

	@Override
	public Object getServiceProvider() {
		return this;
	}

	public void run() {
		do {
			try {
				UIEventHolder holder = uiQueue.poll(1 * 60, TimeUnit.SECONDS);
				if (holder != null) {
					LinkedList<AsyncContext> contexts = null;
					synchronized (requesters) {
						contexts = requesters.remove(holder.id); //
					}
					if (contexts != null)
						synchronized (contexts) {
							for (AsyncContext context : contexts) {
								Collection<UIEvent> events = (Collection<UIEvent>) context.getRequest().getAttribute(
										ATTR_UI_EVENTS);
								if (events == null) {
									events = new LinkedList<UIEvent>();
									context.getRequest().setAttribute(ATTR_UI_EVENTS, events);
								}
								events.add(holder.uiEvent);
								final AsyncContext dc = context;
								context.start(new Runnable() {

									@Override
									public void run() {
										dc.dispatch();
										dc.complete();
									}
								});
								// context.dispatch();
								// context.complete();
							}
						}
					else {
						Thread.sleep(1000);
						if (System.currentTimeMillis() - holder.createdOn < 2 * 60 * 1000)
							addForLate(holder); // reinsert
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				stopped = true;
			} catch (Throwable t) {
				if (t instanceof ThreadDeath)
					throw (ThreadDeath) t;
				t.printStackTrace(); // TODO use logger
			}
		} while (stopped == false);
		requesters = null;
	}

	protected void addForLate(UIEventHolder holder) {
		// insert in deferred processing queue
		// reQueue.add(holder);
		uiQueue.add(holder);
	}

	public void stop() {
		t.interrupt();
	}

	public void addEvent(String id, UIEvent event) {
		UIEventHolder holder = new UIEventHolder();
		holder.id = id;
		holder.uiEvent = event;
		holder.createdOn = System.currentTimeMillis();
		uiQueue.add(holder);
	}

	public void addRequester(String id, AsyncContext context) {
		context.getRequest().setAttribute(SAVED_ID, id);
		context.addListener(this); // not really required
		LinkedList<AsyncContext> currentContexts = null;
		synchronized (requesters) {
			currentContexts = requesters.get(id);
			if (currentContexts == null) {
				currentContexts = new LinkedList<AsyncContext>();
				requesters.put(id, currentContexts);
			}
		}
		synchronized (currentContexts) {
			currentContexts.add(context);
		}
	}
	
	public void dropRequester(String id) {
		LinkedList<AsyncContext> contexts = null;
		synchronized (requesters) {
			contexts = requesters.remove(id); //
		}
		if (contexts != null) {
			synchronized (contexts) {
				for (AsyncContext context : contexts) {
					//context.dispatch();
					context.complete();
				}
			}
		}
	}

	public static class UIEventHolder {
		String id;

		UIEvent uiEvent;

		long createdOn;
	}

	@Override
	public void onComplete(AsyncEvent ae) throws IOException {
	}

	@Override
	public void onError(AsyncEvent ae) throws IOException {
		dequeueRequest(ae.getAsyncContext());
	}

	@Override
	public void onStartAsync(AsyncEvent ae) throws IOException {
	}

	@Override
	public void onTimeout(AsyncEvent ae) throws IOException {
		dequeueRequest(ae.getAsyncContext());
	}

	protected void dequeueRequest(AsyncContext asyncContext) {
		LinkedList<AsyncContext> contexts = null;
	
		synchronized (requesters) {
			try {
				contexts = requesters.get(asyncContext.getRequest().getAttribute(SAVED_ID));
			} catch (Exception e) {

			}			
		}
		synchronized (contexts) {
			contexts.remove(asyncContext);
		}
	}

}
