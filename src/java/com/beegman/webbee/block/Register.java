/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import org.aldan3.data.DODelegator;
import org.aldan3.model.DataObject;
import org.aldan3.model.ProcessException;

import com.beegman.webbee.model.AppModel;

/** Register block is used for a user self sign up procedure. This block is public and has
 * check that new user login is unique. It can also process CAPTCHA. 
 * @author dmitriy
 *
 * @param <T>
 * @param <A>
 */
public class Register<T,A extends AppModel> extends Form<T,A> {
	public static final String TABLE_NAME = "register";

	@Override
	public boolean isPublic() {
		return true;
	}

	@Override
	protected String postValidate(T model) {
		if (isCAPTCHA())
			return "";
		return getResourceString("captcha_error", "Verification code isn't correct");
	}

	@Override
	protected void visit() {
	}

	public String processisLoginAllowedCall() {
		T model = fillModelNoValidation();
		DataObject login = new DODelegator(model) {

			@Override
			public boolean containData(String field) {				
				return getLoginFieldName().equals(field); 
			}

			@Override
			public String getName() {
				return getRegisterTableName();
			}

		};
		try {
			return getAppModel().getDOService().getObjectLike(login) != null?"F":"T";
		} catch (ProcessException pe) {
			log("", pe);
		}
		return "F";
	}

	protected String getRegisterTableName() {
		return TABLE_NAME;
	}
	
	protected String getLoginFieldName() {
		return "login";
	}
	
	protected boolean isCAPTCHA() {
		return true;
	}
}
