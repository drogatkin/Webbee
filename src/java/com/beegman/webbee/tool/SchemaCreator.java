/* **********************************************************************
 * WebBee Copyright 2011 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.tool;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.aldan3.data.DOService;
import org.aldan3.model.Log;
import org.aldan3.model.ProcessException;

//  $Id: SchemaCreator.java,v 1.4 2015/08/25 20:42:51 cvs Exp $

/** this class used to populate schema tables 
 * based model classes
 */
public class SchemaCreator {

	public void create(String packageName, DOService dos) throws ProcessException {
		create(packageName, dos, false, true);
	}
	
	/** creates or update schema
	 * 
	 * @param packageName
	 * @param dos
	 * @param forceDrop
	 * @param update
	 * @throws ProcessException
	 */
	public void create(String packageName, DOService dos, boolean forceDrop, boolean update) throws ProcessException {
		try {
			Class<?>[] modelClasses = getClasses(packageName);
			for (Class modelClass : modelClasses)
				try {
					//System.err.printf("Creating table for %s%n", modelClass);
					TableSet ts = new TableSet(modelClass);
					if (forceDrop)
						try {
							ts.dropTable(dos);
						} catch(ProcessException pe) {
							Log.l.debug("Can't drop table for "+ts+" :"+pe);
						}
					ts.createTable(dos, update);
					Log.l.debug("Created "+(update?"or altered ":"")+" table for: " + modelClass);
				} catch (IllegalArgumentException ie) {
					// skip 
					//Log.l.error("", ie);
				}
		} catch (ProcessException pe) {			
			throw pe;
		} catch (Exception e) {
			throw new ProcessException("Unhandled " + e, e);
		}
	}

	private static Class<?>[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		ArrayList<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			// System.err.printf("Added resource %s%n", resource);
			// TODO make it more robust and actually check for jar protocol
			String resourceFileName = resource.getFile();
			int qp = resourceFileName.indexOf('!');
			if (qp > 0 && resourceFileName.startsWith("file:/"))
				resourceFileName = resourceFileName.substring(6, qp);
			dirs.add(new File(resourceFileName));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	private static ArrayList<Class> findClasses(File directory, String packageName) throws ClassNotFoundException, IOException {
		//System.err.printf("List files from %s%n", directory);
		ArrayList<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					assert !file.getName().contains(".");
					classes.addAll(findClasses(file, packageName + "." + file.getName()));
				} else if (file.getName().endsWith(".class")) {
					classes.add(Class.forName(packageName + '.'
							+ file.getName().substring(0, file.getName().length() - 6)));
				}
			}
		} else if (directory.isFile()) { // try it as jar
			JarFile jarFile = new JarFile(directory);
			Enumeration<JarEntry> entries = jarFile.entries();
			String packagePath = packageName.replace('.', '/');
			int pl = packagePath.length();
			while(entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (!entry.isDirectory()) {
					if (entry.getName().endsWith(".class") &&  entry.getName().startsWith(packagePath)) {
						String className =  entry.getName().substring(pl+1, entry.getName().length() -6);
						//System.err.printf("Entry %s directory %s%n", entry.getName(), className);
						if (className.indexOf('/') < 0)
							classes.add(Class.forName(packageName + '.' + className));
					}
				}				
			}
		}
		return classes;
	}

}
