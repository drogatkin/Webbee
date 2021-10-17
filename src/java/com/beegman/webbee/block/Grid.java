/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.HashMap;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

public class Grid<C,A extends AppModel> extends BaseBlock<A> {
	protected int currentPage;

	@Override
	protected Object doControl() {
		return null;
	}

	@Override
	protected Object getModel() {
		currentPage = getParameterValue(PAGE, 0, 0);
		HashMap pageMap = new HashMap();
		int nr = numRows();
		Object[][] rows = new Object[nr][];
		for (int k=0; k< nr; k++) {
			
			int nc = numCols();
			int w = 100/nc;
			int h = 90/nr;
			Object[] row = new HashMap[nc];
			rows[k] = row;
			for (int i=0; i<nc; i++) {
				HashMap elm = new HashMap();
				elm.put(MODEL, getCellModel(i,k));
				elm.put("x", (i*w)+"%");
				elm.put("y", (k*h)+"%");
				elm.put("w", w+"%");
				elm.put("h", h+"%");
				row[i] = elm;
			}
		}
		pageMap.put("grid", rows);
		pageMap.put(PAGE, currentPage);
		return pageMap;
	}

	@Override
	protected String getSubmitPage() {
		return null;
	}

	protected int numRows() {
		return 3;
	}

	protected int numCols() {
		return 3;
	}
	
	protected int getCurrentPage() {
		return currentPage;
	}
	
	protected C getCellModel(int col, int row) {
		CellModelExample cme = new CellModelExample();
		cme.title= ""+(col*row);
		cme.comment = String.format("This is cell %d,%d", col, row);
		cme.content = "<div>Element<a href=\"#\">nnn</a></div><div>More about</div>";
		return (C) cme;
	}
	
	public static final class CellModelExample {
		public String title;
		public String comment;
		public String content;
	}
}
