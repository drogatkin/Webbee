/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.aldan3.annot.FormField;
import org.aldan3.util.ResourceException;
import org.aldan3.util.ResourceManager;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.util.PageInfo;
import com.beegman.webbee.util.Portlet;

public class Portal<A extends AppModel> extends BaseBlock<A> {

	@Override
	protected Object doControl() {
		return null;
	}

	@Override
	protected Object getModel() {
		HashMap model = new HashMap();
		int np = numberOfPortlets();
		if (np > 0) {
			ArrayList<HashMap> rows = new ArrayList<HashMap>();
			int nc = numberInRow();
			int nr = np / nc;
			int lrc = np % nc;
			if (lrc > 0)
				nr++;
			String pheight = "" + (portalHeightPer() / nr - 1) + "%";
			String pwidth = "" + (portalWidthPer() / nc - 1) + "%";
			int pi = 0;
			for (int r = 0; r < nr; r++) {
				ArrayList<HashMap> cols = new ArrayList<HashMap>();
				if (r == nr - 1) { // last row
					if (lrc > 0 && expandIncompleteRow()) {
						nc = lrc;
						pwidth = "" + (portalWidthPer() / nc - 1) + "%";
					}
				}
				for (int c = 0; c < nc; c++) {
					if (pi >= np)
						throw new RuntimeException("Error in Portal code");
					HashMap pm = new HashMap();
					pm.put("pwidth", pwidth);
					pm.put("pheight", pheight);
					pm.put("pleft", "" + (portalWidthPer() / nc * c) + "%");
					pm.put("ptop", "" + (r * portalHeightPer() / nr + r + 1) + "%");
					addEnv(pm, true);
					fillProtletModel(pm, pi++);
					cols.add(pm);
				}
				HashMap rm = new HashMap();
				rm.put("cols", cols);
				rows.add(rm);
			}
			model.put("rows", rows);
		} else {
			model.put(Variable.ERROR, emptyPortal());
		}
		return model;
	}

	protected boolean expandIncompleteRow() {
		return true;
	}

	protected String emptyPortal() {
		return "No portlets";
	}

	protected ResourceBundle getTextResource(String name) {
		try {
			return (ResourceBundle) getResourceManager(ResourceManager.RESOURCE_RES).getResource(name, this);
		} catch (ResourceException e) {
			log("" + name, e);
		}
		return null;
	}

	////////////  common Ajax handlers   ///////////////////
	public HashMap processrenderportletCall() {
		HashMap pm = new HashMap();
		//addEnv(pm, true);
		fillProtletModel(pm, getParameterValue(Portlet.PORTLET, -1, 0));
		return pm;
	}

	public String getrenderportletViewName() {
		Portlet portlet = get(getParameterValue(Portlet.PORTLET, -1, 0));
		if (portlet != null)
			return portlet.getView();
		return null;
	}

	protected HashMap fillProtletModel(HashMap pm, int i) {
		if (i < 0)
			return null;
		Portlet portlet = get(i);
		if (portlet == null) {
			log("Error: no prtlet for index:"+i, null);
			return null;
		}
		pm.put(Portlet.PORTLET, portlet);
		pm.put(Portlet.PORTLET_ID, i);
		pm.put(Variable.PAGE_TITLE, portlet.getTitle());
		PageInfo pageInfo = portlet.getPagenation();
		if (pageInfo != null) {
			pageInfo.page = getParameterValue(pageInfo.id, 0, 0);
			pm.put(PAGINATION, pageInfo);
		}
		pm.put(MODEL, portlet.loadModel(fillModel(portlet.getModel())));
		pm.put(Portlet.PORTLET_TEMPLATE, portlet.getView());

		ResourceBundle labels = getTextResource(portlet.getPreferredServiceName());
		if (labels == null && useLabels())
			labels = getResource();
		pm.put(Variable.LABEL, labels);
		return pm;
	}

	@Override
	protected String getSubmitPage() {
		return "Portal";
	}

	@Override
	protected boolean reportValidation(String name, String value, Exception problem) {
		return false; // validation for portlets
	}

	/** Override the method to return Portlets definitions for the portal
	 * 
	 * @param i
	 * @return
	 */
	protected Portlet get(int i) {
		switch (i) {
		case 0:
			return new GenericPortlet<List<String>>(Arrays.asList("Orange", "Apple", "Apricot", "Peach", "Cherry",
					"Green bean", "Sunflower seed", "Strawberry", "Plum", "Pear", "Blackcurrant", "Redcurrant",
					"Gooseberry", "Tomato", "Eggplant", "Guava", "Lucuma", "Chili pepper", "Pomegranate", "Kiwifruit",
					"Grape"), "Sweet fruits");
		case 1:
			return new Portlet() {

				@FormField(presentSize = 58)
				public String send_to;

				@FormField(presentRows = 6, presentSize = 58)
				public String message;

				@Override
				public Object getModel() {
					return this;
				}

				@Override
				public String getTitle() {
					return getResourceString("title.formportlet", "title.formportlet");
				}

				@Override
				public String getView() {
					return "portlet/formportlet" + VIEW_EXT;
				}

			};
		}
		return null;
	}

	/** returns total number of portlets
	 * 
	 * @return
	 */
	protected int numberOfPortlets() {
		return 2;
	}

	protected int numberInRow() {
		return 2;
	}

	protected int portalHeightPer() {
		return 98;
	}

	protected int portalWidthPer() {
		return 98;
	}
}