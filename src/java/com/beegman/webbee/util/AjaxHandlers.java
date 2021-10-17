package com.beegman.webbee.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.aldan3.annot.FormField;
import org.aldan3.annot.OptionMap;
import org.aldan3.data.util.FieldConverter;
import org.aldan3.data.util.FieldFiller;
import org.aldan3.model.DataObject;
import org.aldan3.model.Log;

import com.beegman.webbee.base.BaseBlock;
import com.beegman.webbee.model.AppModel;

public class AjaxHandlers {
	public static final String VAR_LABEL = "label";

	public static final String VAR_VALUE = "value";

	public static <T> HashMap commonAjaxProcess(String fieldName, boolean auto, T model, HttpServletRequest request,
			AppModel appModel, TimeZone tz, Locale locale) {
		HashMap pageModel = new HashMap(4);
		try {
			Class<?> modelClass = model.getClass();
			Field field = modelClass.getField(fieldName);
			FormField ff = field.getAnnotation(FormField.class);
			Class actionClass = auto ? ff.recalculateFiller() : ff.presentFiller();
			//System.err.printf("Called with '%s' and %s filler %s%n", fieldName, model, actionClass);
			if (actionClass != FieldFiller.class) {
				FieldFiller ffiller = (FieldFiller) actionClass.newInstance();

				Object result = ffiller.fill(model, fieldName);
				String[] targets = ff.recalculateTargets();
				ArrayList<Option<String,FieldConverter>> converters = new ArrayList<Option<String,FieldConverter>>();
				String[] map = ff.queryResultMap();
				if (targets.length > 0) {
					pageModel.put("target", targets);
					// scan for converters
					for (int ti=0; ti<targets.length; ti++) {
						String target = targets[ti];
						Field tf = modelClass.getField(target);
						FormField tff = tf.getAnnotation(FormField.class);
						if (tff != null && tff.converter() != FieldConverter.class) {
							FieldConverter fc = null;
							try {
								fc = tff.converter().getConstructor(AppModel.class, Field.class)
								.newInstance(appModel, tf);								
							} catch (Exception e) {
								Log.l.error("No 2 params constructor for converter "+ff.converter(), e);
							}
							if (fc == null) {
								fc = tff.converter().newInstance();
							}
							converters.add(new Option<String,FieldConverter>(ti < map.length?map[ti]:target, fc));
						}
					}
				}

				if (result instanceof Collection)
					result = ((Collection) result).toArray();
				if (result instanceof Object[]) {
					Object[] els = (Object[]) result;
					for (int oi = 0; oi < els.length; oi++) {
						// TODO do target replacements
						if (els[oi] instanceof DataObject == false)
							els[oi] = new DODelegatorEx(els[oi]);
						for (Option<String,FieldConverter> o:converters) { 
							//System.err.printf("--> %s == %s%n", ((DataObject)els[oi]).get(o.id), o.id);
							((DataObject)els[oi]).modifyField(o.id, o.label.deConvert(((DataObject)els[oi]).get(o.id), tz, locale));							
						}
					}
				}
				pageModel.put(BaseBlock.MODEL, result);
				
				if (map.length == 0) {
					map = new String[2];
					OptionMap om = (OptionMap) actionClass.getAnnotation(OptionMap.class);
					if (om != null) {
						if (om.valueMap().length() > 0)
							map[0] = om.valueMap();
						else
							map[0] = VAR_VALUE;
						if (om.labelMap().length() > 0)
							map[1] = om.labelMap();
						else
							map[1] = VAR_LABEL;
					} else {
						map[0] = VAR_VALUE;
						map[1] = VAR_LABEL;
					}
				}
				pageModel.put("mapping", map);
			}
		} catch (Exception e) {e.printStackTrace();
			Log.l.error(String.format("Exception %s in %s of %s", e, auto ? "autosuggest" : "cascading", fieldName), e);
		}
		return pageModel;
	}
}
