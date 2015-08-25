package com.beegman.webbee.block;

import org.aldan3.model.ProcessException;

import com.beegman.webbee.model.AppModel;
import com.beegman.webbee.model.Setup;
import com.beegman.webbee.tool.SchemaCreator;

public class Systemsetup<T extends Setup, A extends AppModel> extends Form<T, A> {

	protected String getDefaultModelPackage() {
		return Setup.class.getPackage().getName();
	}
	
	@Override
	protected T getFormModel() {
		return (T) new Setup();
	}

	@Override
	protected void loadModel(T setup) {
		setup.model_package_name = getDefaultModelPackage();
	}

	@Override
	protected Object storeModel(T setup) {
		try {
			new SchemaCreator().create(setup.model_package_name, getAppModel().getDOService(), setup.discardExisting, true);
		} catch (ProcessException e) {
			log("", e);
			return "Problem: "+e;
		}
		return "";
	}
}
