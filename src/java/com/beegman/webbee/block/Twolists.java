/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
//  $Id: Twolists.java,v 1.4 2011/03/30 01:14:22 dmitriy Exp $
package com.beegman.webbee.block;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpSession;

import org.aldan3.annot.FormField;
import org.aldan3.model.ProcessException;
import org.aldan3.model.TemplateProcessor;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.util.Option;

public class Twolists<S, T, A extends AppModel> extends BaseBlock<A> {

	@Override
	protected Object doControl() {
		return null;
	}

	@Override
	protected Object getModel() {
		HashMap pageMap = new HashMap();
		HashMap sm = new HashMap();
		sm.put("view", getSourceListView());
		sm.put(MODEL, getSourceListModel());
		pageMap.put("src_ctrl", sm);
		sm = new HashMap();
		sm.put("view", getTargetListView());
		sm.put(MODEL, getTargetListModel());
		pageMap.put("trg_ctrl", sm);

		return pageMap;
	}

	@Override
	protected String getSubmitPage() {
		return navigation;
	}

	@Override
	protected String getContentType(String viewName) {
		if (viewName.endsWith(".json"))
			return "text/plain; charset=UTF-8";
		return null;
	}

	/** Override this method providing a model to render in a source list
	 * 
	 * @return
	 */
	protected S getSourceListModel() {
		S sm = getSessionAttribute("source_model", (S) null);
		if (sm == null) {
			HttpSession s = req.getSession();
			s.setAttribute("source_model", sm = (S) makePrefilledList());
		}

		return sm;
	}

	/** Override this method providing a model to render in a target list
	 * 
	 * @return
	 */
	protected T getTargetListModel() {
		T tm = getSessionAttribute("target_model", (T) null);
		if (tm == null) {
			HttpSession s = req.getSession();
			s.setAttribute("target_model", tm = (T) new ArrayList());
		}

		return tm;
	}

	/** Override this method to do changes when source list was modified
	 * 
	 */
	protected void doSourceControl() {
		String[] ids = req.getParameterValues("id");
		if (ids != null) {
			S sm = getSourceListModel();
			T tm = getTargetListModel();
			Iterator i = ((ArrayList) sm).iterator();
			while (i.hasNext()) {
				Option o = (Option) i.next();
				for (String id : ids) {
					if (id.equals("" + o.id)) {
						((ArrayList) tm).add(o);
						i.remove();
						break;
					}
				}
			}
		}
	}

	/** Override this method to do changes when target list was modified
	 * 
	 */
	protected void doTargetControl() {
		String[] ids = req.getParameterValues("id");
		if (ids != null) {
			S sm = getSourceListModel();
			T tm = getTargetListModel();
			Iterator i = ((ArrayList) tm).iterator();
			while (i.hasNext()) {
				Option o = (Option) i.next();
				for (String id : ids) {
					if (id.equals("" + o.id)) {
						((ArrayList) sm).add(o);
						i.remove();
						break;
					}
				}
			}
		}
	}

	/** Override this method to provide custom view for source list
	 * 
	 * @return
	 */
	protected String getSourceListView() {
		return "insert/simplelist.htmt";
	}

	/** Override this method to provide custom view for target list
	 * 
	 * @return
	 */
	protected String getTargetListView() {
		return "insert/simplelist.htmt";
	}

	//////////////////   for pagenation //////////////////
	protected int getSourcePage() {
		return 0;
	}

	protected int getSourcePageSize() {
		return -1;
	}

	protected int getTargetPage() {
		return 0;
	}

	protected int getTargetPageSize() {
		return -1;
	}

	@Override
	protected TemplateProcessor getTemplateProcessor(String view) {
		if (view.endsWith(".json"))
			return getAppModel().getTemplateProcessor();
		return super.getTemplateProcessor(view);
	}

	// {src:"@^src@",trg:"@^trg@"}
	public Object processUpdateviewsCall() {
		HashMap result = new HashMap();
		lec = new ListsExchangeCtrl();
		fillModel(lec);

		if (lec.submit != 0)
			if (lec.source_view)
				doSourceControl();
			else
				doTargetControl();
		TemplateProcessor tp = getAppModel().getTemplateProcessor();
		HashMap wm = new HashMap();
		try {
			wm.put(MODEL, getSourceListModel());
			StringWriter sw = new StringWriter(1024 * 4);
			tp.process(sw, getSourceListView(), wm, getProperties(), getLocale(), getTimeZone());
			result.put("src", sw);
			wm.clear();
			wm.put(MODEL, getTargetListModel());
			sw = new StringWriter(1024 * 4);
			tp.process(sw, getTargetListView(), wm, getProperties(), getLocale(), getTimeZone());
			result.put("trg", sw);
		} catch (ProcessException pe) {
			log("", pe);
		}
		return result;
	}

	public String getUpdateviewsViewName() {
		return "insert/twolists.json";
		//return lec.source_view ? getSourceListView() : getTargetListView();
	}

	////////////////////////  methods for demo purpose only //////////////////////

	ArrayList makePrefilledList() {
		ArrayList result = new ArrayList();
		result.add(new Option(1, "Cabage"));
		result.add(new Option(2, "Onion"));
		result.add(new Option(3, "Potato"));
		result.add(new Option(4, "Garlic"));
		result.add(new Option(5, "Tomato"));
		result.add(new Option(6, "Cucumber"));
		result.add(new Option(7, "Celantro"));
		result.add(new Option(8, "Vinegar"));
		result.add(new Option(9, "Myoneze"));
		result.add(new Option(10, "Oil"));
		return result;
	}

	protected ListsExchangeCtrl lec;

	public static final class ListsExchangeCtrl {
		@FormField
		public boolean source_view;

		@FormField(formFieldName = "submit.x")
		public int submit;
	}

}
