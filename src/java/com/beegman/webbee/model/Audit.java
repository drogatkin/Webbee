package com.beegman.webbee.model;

import java.util.Date;

import org.aldan3.annot.DBField;
import org.aldan3.annot.DataRelation;

import com.beegman.webbee.util.SimpleCoordinator;
/** model ready for audit table and corresponding activities
 * 
 * @author dmitriy
 *
 * @param <T>
 */
@DataRelation(table="audit")
public class Audit<T extends AppModel> extends SimpleCoordinator <T>{

	public Audit(T model) {
		super(model);
	}
	
	@DBField(key = true, auto = 1)
	public long id;
	
	@DBField(index=true)
	public char operation;
	
	@DBField(index=true)	
	public long by_requester;
	
	@DBField(index=true)
	public long object;
	
	@DBField(index=true)
	public int type;
	
	@DBField(index=true, auto=-1)
	public Date stamp;

}
