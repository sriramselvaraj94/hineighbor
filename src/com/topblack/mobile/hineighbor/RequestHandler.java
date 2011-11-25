/**
 * File:    RequestHandler.java
 * Author : 10115154
 * Created: Nov 11, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

/**
 * @author 10115154
 *
 */
public abstract class RequestHandler {
	
	protected final String LOG_TAG = this.getClass().getSimpleName(); 
	
	protected ClientProcessor processor = null;
	
	public RequestHandler(ClientProcessor processor) {
		this.processor = processor;
	}
	
	public abstract int handleRequest(byte[] body) throws Exception;
}
