package com.beegman.webbee.block;

import com.beegman.webbee.base.BaseBlock;
import org.aldan3.model.TemplateProcessor;
import org.aldan3.servlet.Constant;
import com.beegman.webbee.model.AppModel;

public class Spa<A extends AppModel> extends BaseBlock<A> {

	@Override
	protected TemplateProcessor getTemplateProcessor(String viewName) {
		// TODO this maybe overkill
	   if (true || isSPA()) {
		   req.removeAttribute(Constant.Request.INNER_VIEW); // preventing render an actual view
	   }
	   return super.getTemplateProcessor( viewName);
	}
	
	@Override
	protected boolean isSPA() {
		return false;
	}
	
	@Override
	protected Object getModel() {
		return EMPTYMAP;
	}
	
	@Override
	protected String getCanvasView() {
		// TODO maybe separate for mobile?
		log("called for canvas", null);
		return super.getCanvasView();
		/*return getConfigValue(isPublic() ? CONFIG_PUBLIC_CANVAS : CONFIG_CANVAS, "canvas")
				+ getConfigValue(CONFIG_VIEWEXT, VIEW_EXT);*/
	}

}
