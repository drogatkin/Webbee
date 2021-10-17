/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

import org.aldan3.annot.FormField;
import org.aldan3.util.inet.HttpUtils;

public class ajax {
	public static String initHandlers(Object model) {
		if (model == null)
			return "function initializeFieldsHandlers() {}\n";
		Field[] flds = model.getClass().getFields();
		HashMap<String, HashSet<String>> dependentFF = new HashMap<String, HashSet<String>>(10);
		StringBuffer js = new StringBuffer(256);
		StringBuffer asjs = new StringBuffer(256);
		StringBuffer attachjs = new StringBuffer(256);
		js.append("var collecMap = {");
		boolean firstG = true;
		for (Field f : flds) {
			FormField ff = f.getAnnotation(FormField.class);
			if (ff != null) {
				if (ff.dependencies().length > 0) {
					if (firstG == false)
						js.append(',');
					firstG = false;
					js.append("'").append(f.getName()).append("':[");
					boolean firstF = true;
					for (String fn : ff.dependencies()) {
						HashSet<String> deps = dependentFF.get(fn);
						if (deps == null) {
							deps = new HashSet<String>();
							dependentFF.put(fn, deps);
						}
						deps.add(f.getName());
						if (firstF == false)
							js.append(',');
						firstF = false;
						js.append("'").append(fn).append("'");
					}
					js.append("]");
				}
				if (ff.autosuggest()) {
					asjs.append("addAutosuggestHandler('").append(f.getName()).append("');");
				}
				
				if (ff.presentType() == FormField.FieldType.File) { 
					attachjs.append("addAttchHandler('").append(ff.formFieldName().length() > 0?ff.formFieldName():f.getName());
					if (ff.fillQuery().length() > 0)
						attachjs.append("', '").append(ff.fillQuery());
					attachjs.append("');");	
				}
			}
		}
		js.append("};\n");
		js.append("function initializeFieldsHandlers() {");
		js.append(asjs);
		js.append(" initCommonAutosuggestHandlers();\n");
		for (String fn : dependentFF.keySet()) {
			Field f;
			try {
				f = model.getClass().getField(fn);
				FormField ff = f.getAnnotation(FormField.class);
				if (ff != null) {
					String an = ff.formFieldName().length() == 0 ? fn : HttpUtils.toJavaString(ff.formFieldName());
					for (String updatedField : dependentFF.get(fn)) {
						Field f1 = model.getClass().getField(updatedField);
						FormField ff1 = f1.getAnnotation(FormField.class);
						// no check ff1 != null since already checked
						if (ff1.autosuggest() == false)
							js.append(" addCascadeHandler('").append(an).append("','").append(ff1.formFieldName().length() == 0 ? updatedField : HttpUtils.toJavaString(ff1.formFieldName())).append("');");
					}
				} else
					js.append(String.format("//  The field %s listed in dependency isn't form field, check spelling",
							fn));
			} catch (SecurityException e) {
				js.append(String.format("// Can't access field %s, %s", fn, e));
			} catch (NoSuchFieldException e) {
				js.append(String.format("// No such field %s, %s", fn, e));
			}
		}
		js.append(attachjs);
		js.append("}");		
		return js.toString();
	}
	
	public static String initialCascading(Object model) {
		StringBuffer js = new StringBuffer(256);
		js.append(" function initialCascading() {");
		js.append(" }\n");
		return js.toString();
	}

	public static String submitValidation(Object model) {
		StringBuffer js = new StringBuffer(256);
		
		return js.toString();
	}
	
	public static String initGadgets( Object model) {
		return "var gadgetHandlers = new Array();\r\n";
	}
}
