/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.aldan3.annot.Behavior;
import org.aldan3.annot.DBField;
import org.aldan3.annot.FormField;
import org.aldan3.annot.FormField.FieldType;
import org.aldan3.annot.RequiresOverride;
import org.aldan3.model.DataObject;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.util.AjaxHandlers;
import com.beegman.webbee.util.DODelegatorEx;
import com.beegman.webbee.util.GenericResourceOptions;
import com.beegman.webbee.util.PageInfo;
import com.beegman.webbee.util.SimpleCoordinator;

/** Used for display tabular information with edit capabilities
 * 
 * @author Dmitriy
 *
 * @param <A>
 */
@Behavior(templatePattern = ".*json|.*htm.?")
public class Editabletabular<A extends AppModel, I, R, S> extends BaseBlock<A> {

	public static final class MovieTitle extends SimpleCoordinator {
		public MovieTitle(AppModel model) {
			super(model);
			id = -1;
		}

		@DBField
		@FormField(presentType = FieldType.Hidden)
		public int id;

		@DBField
		@FormField
		public String name;

		@DBField
		@FormField
		public String description;

		@DBField
		@FormField
		public String director;

		@DBField
		@FormField
		public String actors;

		@DBField
		@FormField(presentFiller = GenericResourceOptions.class)
		public int genre;

		@DBField
		@FormField
		public int duration;

		@DBField
		@FormField(presentFiller = GenericResourceOptions.class)
		public int rate; // supposes to be rating

	}

	public static final class MovieList extends SimpleCoordinator {
		public MovieList(AppModel model) {
			super(model);
			listId = "" + Math.random();
		}

		@DBField
		@FormField(presentType = FieldType.Hidden)
		public String listId;

		@DBField
		@FormField
		public String listName;

		@DBField
		@FormField(presentFiller = GenericResourceOptions.class)
		public int listType;

		public ArrayList<MovieTitle> movies;

		public String toString() {
			return "Movies list: (" + listId + ") '" + listName + "' of " + listType + " size "
					+ (movies == null ? -1 : movies.size());
		}
	}

	private boolean enableValidation;

	@Override
	protected Object doControl() {
		//
		enableValidation = true;
		I headerModel = getHeaderModel(null);
		fillModel(headerModel);
		storeModel(headerModel);
//		navigation = "Editabletabular?listId=" + ((MovieList) headerModel).listId; // TODO URI component encode
		return null;
	}

	@Override
	protected Object getModel() {
		HashMap result = new HashMap();
		I headerModel = getHeaderModel(null);
		enableValidation = false;
		fillModel(headerModel);
		result.put(MODEL, headerModel = getHeaderModel(headerModel));
		PageInfo pageInfo = getPagenation(headerModel);
		// TODO make prefix constants
		result.put("rows_" + MODEL, getTabularModel(headerModel, pageInfo.pageSize * pageInfo.page, pageInfo.pageSize));
		result.put("row_" + MODEL, getRowModel(null, headerModel));
		return result;
	}

	@Override
	protected String getSubmitPage() {
		return navigation;
	}

	@Override
	protected boolean reportValidation(String name, String value, Exception problem) {
		if (enableValidation)
			return super.reportValidation(name, value, problem);

		return false;
	}

	/** method is  used for organization pagination in long lists
	 * 
	 * @param headerModel
	 * @return
	 */
	protected PageInfo getPagenation(I headerModel) { // TODO move the method in base block
		PageInfo result = new PageInfo(-1, -1, "");
		result.page = 0;
		return result;
	}

	@RequiresOverride
	protected R getTabularModel(I headerModel, long start, int size) {
		if (headerModel == null || ((MovieList) headerModel).movies == null)
			return null;
		ArrayList<MovieTitle> result = new ArrayList<MovieTitle>(size > 0 ? size
				: ((MovieList) headerModel).movies.size());
		if (size > 0) {
			for (int i = (int) start; i < start + size; i++)
				result.add(((MovieList) headerModel).movies.get(i));
		} else
			result = ((MovieList) headerModel).movies;
		return (R) result;
	}

	@RequiresOverride
	protected I getHeaderModel(I headerModel) {
		//log("Header model :%s", null, headerModel);
		if (headerModel == null || ((MovieList) headerModel).listId.length() == 0)
			return (I) new MovieList(getAppModel());
		I result = (I) getList(((MovieList) headerModel).listId);
		if (result == null) {
			putList(((MovieList) headerModel).listId, (MovieList) headerModel);
			return headerModel;
		}
		return result;
	}

	/** returns row object for editing
	 * <p>
	 * the method must be overridden
	 * 
	 * @param rowId
	 * @return
	 */
	@RequiresOverride
	protected S getRowModel(Object rowId, I headerModel) {
		//log("===> row id: %s for %s", null, rowId, headerModel);
		MovieList ml = headerModel == null ? null : getList(((MovieList) headerModel).listId);
		if (rowId == null || (Integer) rowId < 0 || ml == null || ml.movies == null
				|| ml.movies.size() - 1 < (Integer) rowId)
			return (S) new MovieTitle(getAppModel());
		return (S) ml.movies.get((Integer) rowId);
	}

	/** saves model encapsulated tabular model
	 * 
	 * @param headerModel
	 */
	@RequiresOverride
	protected void storeModel(I headerModel) {
		//log("Store model " + headerModel, null);
		I currentModel = getHeaderModel(headerModel);
		if (currentModel != null) {
			((MovieList) headerModel).movies = ((MovieList) currentModel).movies;
		}
		if (headerModel != null && ((MovieList) headerModel).listId.length() > 0)
			putList(((MovieList) headerModel).listId, (MovieList) headerModel);
	}

	@RequiresOverride
	protected void storeRowModel(S rowModel, Object rowId, I headerModel) {
		//log("+++++++store r:%s at %s in %s", null, rowModel, rowId, headerModel);
		if (headerModel != null) {
			MovieList ml = (MovieList) headerModel;
			if (ml.movies == null)
				ml.movies = new ArrayList<MovieTitle>();
			if (rowId != null) {
				int r = (Integer) rowId;
				if (r != ((MovieTitle) rowModel).id)
					//log("!!!!!!!!!Inconsitency %d vs %d", null, r, ((MovieTitle) rowModel).id);
					if (r >= 0 && r < ml.movies.size()) {
						//log("<<<<<<<<<>>>>>>>updateing %d", null, r);
						ml.movies.set(r, (MovieTitle) rowModel);
						return;
					}
			}

			((MovieTitle) rowModel).id = ml.movies.size();
			//log("<<<<<<<<<>>>>>>>updateing %d", null, ((MovieTitle) rowModel).id);
			ml.movies.add((MovieTitle) rowModel);
		}
	}

	@RequiresOverride
	protected void deleteRow(Object rowId, I headerModel) {
		if (headerModel != null) {
			MovieList ml = (MovieList) headerModel;
			if (rowId != null && ml.movies != null) {
				int r = (Integer) rowId;
				int c = 0;
				Iterator<S> mi = (Iterator<S>) ml.movies.iterator();
				while (mi.hasNext()) {
					MovieTitle mt = (MovieTitle) mi.next();
					if (mt.id == r) {
						mi.remove();
					} else {
						mt.id = c++;
					}
				}
			}
		}
	}

	@RequiresOverride
	protected Object extractRowId() {
		return getParameterValue("rowId", -1, 0);
	}

	////////////////////  demonstration method only ////////////////////////
	MovieList getList(String name) {
		return getSessionAttribute("^^list_" + name, (MovieList) null);
	}

	void putList(String name, MovieList list) {
		req.getSession(false).setAttribute("^^list_" + name, list);
	}

	////////////////////  Ajax handlers  ////////////////////////

	public HashMap<String, DataObject> processeditRowCall() {

		HashMap<String, DataObject> result = new HashMap<String, DataObject>(2);
		I headerModel = getHeaderModel(null);
		enableValidation = false;
		fillModel(headerModel);

		result.put(MODEL, new DODelegatorEx(getRowModel(extractRowId(), headerModel)));
		//log("fields:%s", null, ((DODelegator)result.get(MODEL)).getFieldNames());
		return result;
	}

	public String geteditRowViewName() {
		return "insert/dataobject.json";
	}

	/** updates display row information accordingly editing
	 * 
	 * @return
	 */
	public HashMap<String, DataObject> processupdateRowCall() {
		I headerModel = getHeaderModel(null);
		enableValidation = true;
		fillModel(headerModel);
		headerModel = getHeaderModel(headerModel);
		Object rowId = extractRowId();
		S rowModel = getRowModel(rowId, headerModel);
		fillModel(rowModel);
		storeRowModel(rowModel, rowId, headerModel);
		//storeModel(headerModel);
		HashMap<String, DataObject> result = new HashMap<String, DataObject>(2);
		result.put(MODEL, new DODelegatorEx<S>(rowModel));
		return result;
	}

	public String getupdateRowViewName() {
		return "insert/dataobject.json";
	}

	public String processdeleteRowCall() {
		I headerModel = getHeaderModel(null);
		enableValidation = true;
		fillModel(headerModel);
		deleteRow(extractRowId(), headerModel);
		return "Ok";
	}

	public Object processAutosuggestCall() {
		enableValidation = false;

		return AjaxHandlers.commonAjaxProcess(getParameterValue("autosuggest", "", 0), true, fillModel("editable"
				.equals(getParameterValue("formname", "", 0)) ? getRowModel(null, null) : getHeaderModel(null)), req, getAppModel(), getTimeZone(), getLocale());
	}
}
