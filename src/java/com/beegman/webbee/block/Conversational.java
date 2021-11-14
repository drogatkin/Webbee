package com.beegman.webbee.block;

import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

public class Conversational<I, O, A extends AppModel> extends Restful<I,O,A> {
	
	protected O process(I ask) {
	return null;	
	}
	
	@Override
	protected Object getModel() {
		Object result = process(readModel());
		//log("result: %s", null, result);
		resp.setStatus(returnCode);
		if (noTemplate()) {
			log("return %s", null, result);
			return result;
		}
		HashMap<String, Object> pageModel = new HashMap<String, Object>(10);
		pageModel.put(MODEL, result);
		return pageModel;
	} 
	
	@Override
	protected void start() {
		super.start();
	    if (restful_op.Create != op) { 
	    	returnCode = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
	    }
	}

	@Override
	protected Object doControl() {
		throw new UnsupportedOperationException("Here is no controll for the exchanges service");
	}
	
}
