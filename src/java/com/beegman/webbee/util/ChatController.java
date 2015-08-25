/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.aldan3.model.ServiceProvider;

public class ChatController implements ServiceProvider {
	LinkedHashMap<String, ChatInfo> participants;

	public static final String NAME = "ChatController";

	@Override
	public String getPreferredServiceName() {
		return NAME;
	}

	@Override
	public Object getServiceProvider() {
		return this;
	}

	public ChatController() {
		participants = new LinkedHashMap<String, ChatInfo>();
	}

	public void register(String name, String uiid) {
		// TODO add success code
		ChatInfo ci = new ChatInfo();
		ci.name = name;
		ci.uiid = uiid;
		ci.available = true;
		synchronized (participants) {
			participants.put(name, ci);
		}
	}
	
	public boolean isAvailable(String name) {
		ChatInfo ci = participants.get(name);
		return (ci != null && ci.available && ci.inprogess == false);
	}
	
	public void unregister(String name) {
		//System.err.printf("!!!!!!!!UNREGISTER!!! %s%n", name);
		// TODO maybe just update flag
		if (participants.remove(name) == null );
	}

	public String getId(String name) {
		ChatInfo ci = participants.get(name);
		//System.err.printf("================> %s  -> %s%n", ci, ci==null?null:ci.uiid);
		if (ci != null)
			return ci.uiid;
		return null;
	}

	public Collection<String> getAvailable() {
		LinkedList<String> result = new LinkedList<String>();
		for (String name : participants.keySet()) {
			ChatInfo ci = participants.get(name);
			if (ci.available && ci.inprogess == false)
				result.add(name);
		}
		return result;
	}

	public static class ChatInfo {
		String name;

		boolean available;

		boolean inprogess;

		String uiid;
	}

}
