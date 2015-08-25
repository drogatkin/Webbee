/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.HashMap;

import com.beegman.webbee.base.BaseBlock;

public class Message extends BaseBlock {
	
	/** Override to provide action on approve
	 * 
	 */
	protected void doApprovedAction() {

	}

	/** Override to provide action on cancel
	 * 
	 */
	protected void doCancelAction() {

	}

	/** Override this method to carry payload in a message, unless you can do it in session
	 * 
	 * @return
	 */
	protected String getPayload() {
		return null;
	}

	/** Override to customize title
	 * 
	 */
	@Override
	protected String getTitle() {
		return "Message";
	}

	/** Override to customize message
	 * 
	 * @return
	 */
	protected String getMessage() {
		return "Please confirm";
	}

	@Override
	protected Object doControl() {
		String submit = getParameterValue(Form.SUBMIT, "", 0);
		if ("Confirm".equals(submit))
			doApprovedAction();
		else if ("Cancel".equals(submit))
			doCancelAction();
		else
			log("Not supported operation:"+submit, null);
		navigation = getParameterValue("caller", "", 0);
		return null;
	}

	@Override
	protected Object getModel() {
		HashMap page = new HashMap();
		MessageModel model = new MessageModel();
		model.title = getTitle();
		model.message = getMessage();
		model.payload = getPayload();
		page.put(MODEL, model);
		page.put("caller", req.getAttribute("javax.servlet.forward.request_uri"));
		page.put("action", getPreferredServiceName());
		return page;
	}

	@Override
	protected String getSubmitPage() {
		return navigation;
	}
	
	@Override
	protected boolean useLabels() {
		return false;
	}

	public static class MessageModel {
		public String title;
		public String message;
		public String payload;
	}
}
