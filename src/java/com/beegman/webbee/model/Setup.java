package com.beegman.webbee.model;

import org.aldan3.annot.FormField;

public class Setup {
	@FormField
	public String model_package_name;
	
	@FormField
	public boolean discardExisting;
	
	@FormField
	public boolean doAlter;
}
