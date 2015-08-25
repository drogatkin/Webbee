/* **********************************************************************
 * WebBee Copyright 2009 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.io.Serializable;

public class History implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "##" + History.class.getName();

	private int maxSize;
	
	private int preservedHead;
	
	private LinkedList<PageRef> history = new LinkedList<PageRef>();

	///// control methods  /////
	public void setMaxSize(int maxsize) {
		maxSize = maxsize;
	}
	
	/** when new entry is added and size exceeds limits, oldest element get removed, however
	 * certain number of elements is preserved in head
	 * 
	 */
	public void setPresevedHead(int preseved) {
		preservedHead = preseved;
	}
	
	public void push(PageRef ref) {
		//Iterator<PageRef> i = history.descendingIterator();
		// look if it is in history, then just short history to previous entry
		int i = history.lastIndexOf(ref);
		
		if (i >= 0) {
			pop(history.size()-i-1);
		} else {
			if (maxSize >0 && history.size() == maxSize)
				history.remove(preservedHead);
			history.addLast(ref);
		}
	}

	public PageRef pop(int deep) {
		if (deep <= 0)
			return null;
		PageRef result = null;
		try {
			for (int i=0;history.isEmpty() == false && i<deep;i++)
				result = history.removeLast();
		} catch (IndexOutOfBoundsException iobe) {

		} catch (NoSuchElementException nse) {
			
		}
		return result;
	}
	
	/** returns size of history
	 * 
	 * @return
	 */
	public int getSize() {
		return history.size();
	}
	
	public Iterator<PageRef> iterator() {
		return history.iterator();
	}
}
