/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.aldan3.servlet.Constant;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.model.UIEvent;
import com.beegman.webbee.util.ChatController;

// TODO extend Gadget
@ServerEndpoint("/chat/{participant}")
public class Chat<A extends AppModel> extends BaseBlock<A> {

	@Override
	protected Object doControl() {
		return null;
	}

	@Override
	protected Object getModel() {
		processavailableCall();
		HashMap page = new HashMap();
		page.put("names", getParticipantNames());

		return page;
	}

	@Override
	protected String getSubmitPage() {
		return null;
	}

	@Override
	protected void addExtraHeader(Map mapModel, boolean ajaxView) {
		if (ajaxView == false)
			mapModel.put("headextra", "insert/chat_scripts.htmt");
	}

	protected Collection<String> getParticipantNames() {
		return getChatController().getAvailable();
	}

	protected String getParticipantName() {
		return getSessionAttribute(Constant.Variable.PAGE_TITLE, (String) null);
	}

	protected void register(String name, String id) {
		ChatController cc = getChatController();
		cc.register(name, id);
	}

	protected void unregister(String name) {
		ChatController cc = getChatController();
		cc.unregister(name);
	}

	protected ChatController getChatController() {
		ChatController result = (ChatController) frontController.getAttribute(ChatController.NAME);
		if (result == null) {
			synchronized (frontController) {
				result = (ChatController) frontController.getAttribute(ChatController.NAME);
				if (result == null)
					result = new ChatController();
				frontController.getServletContext().setAttribute(ChatController.NAME, result);
			}
		}
		return result;
	}

	//////////////////////////  Ajax handlers ////////////////////////////
	public String processavailableCall() {
		register(getParticipantName(), getUIID());
		return "ok";
	}

	public String processunavailableCall() {
		unregister(getParticipantName());
		// TODO send 'closeChat' to all being in chat with
		return "ok";
	}

	public String processinitiateCall() {
		String participant = getParameterValue("participant", "", 0);
		if (participant.length() == 0)
			return "error";
		log("initiated with participant:" + participant, null);
		String puiid = getChatController().getId(participant);
		if (puiid != null) {
			UIEvent uie = new UIEvent();
			uie.eventHandler = "initiateChat";
			uie.parameters = new Object[3];
			uie.parameters[0] = getParticipantName();
			uie.parameters[1] = getUIID();
			uie.parameters[2] = getResourceString("me", "Me");
			pushUIEvent(uie, puiid);
			return "ok";
		}
		return "error";
	}

	public String processmessageCall() {
		String participant = getParameterValue("participant", "", 0);
		log("participant:" + participant, null);
		if (participant.length() == 0)
			return "error";
		String puiid = getChatController().getId(participant);
		if (puiid != null) {
			UIEvent uie = new UIEvent();
			uie.eventHandler = "chatMessage";
			uie.parameters = new Object[3];
			uie.parameters[0] = getParticipantName();
			uie.parameters[1] = getUIID();
			uie.parameters[2] = getParameterValue("message", "", 0).trim();
			log("chat message:" + uie.parameters[2] + ", from:" + uie.parameters[0], null);
			pushUIEvent(uie, puiid);
			return "ok";
		}
		return "error";
	}

	public String processterminateCall() {
		String participant = getParameterValue("participant", "", 0);
		log("participant:" + participant, null);
		if (participant.length() > 0) {
			String puiid = getChatController().getId(participant);
			if (puiid != null) {
				UIEvent uie = new UIEvent();
				uie.eventHandler = "terminateChat";
				uie.parameters = new Object[2];
				uie.parameters[0] = getParticipantName();
				uie.parameters[1] = getUIID();
				pushUIEvent(uie, puiid);
				return "ok";
			}
		}
		return "error";
	}
	
	@OnMessage()
	public void messagesProcessing(String message, javax.websocket.Session ses, @PathParam("participant") String participant) throws IOException, EncodeException {
		for(javax.websocket.Session s:ses.getOpenSessions()) {
			if (participant.equals(s.getUserProperties().get("participant"))) {
				UIEvent uie = new UIEvent();
				uie.eventHandler = "chatMessage";
				uie.parameters = new Object[] {getParticipantName(), message};
					s.getBasicRemote().sendObject(uie);
			}
		}
	}
	
	@OnOpen
	public void register() {
		
	}
}
