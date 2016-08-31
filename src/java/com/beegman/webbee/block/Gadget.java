/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.HashMap;

import org.aldan3.annot.RequiresOverride;
import org.aldan3.model.TemplateProcessor;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

/** This class is used to put small dynamically changed
 * gadget to a page canvas. No page reload required for updating gadget state and information
 * @author Dmitriy
 *
 * @param <G,A>
 */
public class Gadget<G, A extends AppModel> extends BaseBlock<A> {

	@Override
	protected Object doControl() {
		throw new UnsupportedOperationException("Control isn't supported for gadgets");
	}

	@Override
	protected Object getModel() {
		HashMap<String, Object> gadgetModel = new HashMap<String, Object>();
		gadgetModel.put(MODEL, getGadgetData());
		return gadgetModel;
	}

	@Override
	protected String getSubmitPage() {
		throw new UnsupportedOperationException("Control isn't supported for gadgets");
	}
	
	@Override
	protected void visit() {

	}
	
	@Override
	protected String getCanvasView() {
		return null;
	}

	@Override
	protected TemplateProcessor getTemplateProcessor(String view) {
		if (view.endsWith(".json"))
			return getAppModel().getTemplateProcessor();
		return super.getTemplateProcessor(view);
	}

	/** override this methof to generate gadget data
	 * 
	 * @return
	 */
	@RequiresOverride
	protected G getGadgetData() {
		return null;
	}
	
	/** override this method is gadget should return something different than JSON
	 * 
	 * @return
	 */
	protected String getType() {
		return ".json";
	}

	@Override
	protected String getViewName() {
		return "gadget/"+getResourceName()+getType();
	}

	@Override
	protected boolean useLabels() {
		return false;
	}
	
}
