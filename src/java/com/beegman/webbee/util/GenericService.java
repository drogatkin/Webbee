package com.beegman.webbee.util;

import java.util.concurrent.ThreadPoolExecutor;

import org.aldan3.model.ServiceProvider;

import com.beegman.webbee.model.AppModel;

/** This class provides a a generic template fro creation any service
 * 
 * @author dmitriy
 *
 * @param <S>
 * @param <A>
 */
public abstract  class GenericService<S extends ServiceProvider, A extends AppModel>  implements ServiceProvider<S>, Runnable  {
	enum ServStat {running, stopped, suspended, failed, paused };
	
	protected A appModel;
	
	public GenericService(A app) {
		appModel = app;
	}
	
	public abstract void doService();

	@Override
	public void run() {
		doService();
	}

	@Override
	public String getPreferredServiceName() {
		return getClass().getName();
	}

	@Override
	public S getServiceProvider() {
		return (S) this;
	}
	

	public ServStat getState() {
		return ServStat.stopped;
	}
	
	public void start() {
		new Thread(this).start();
	}
	
	public void stop() {
		
	}
	
	public void pause() {
	}
	
	public void resume() {
	}
	
}
