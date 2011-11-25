/**
 * File:    ControlRequestHandler.java
 * Author : 10115154
 * Created: Nov 11, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import android.util.Log;


/**
 * @author 10115154
 *
 */
public class ChatMessageHandler extends RequestHandler {

	private static final String LOG_TAG = ChatMessageHandler.class.getSimpleName();
	
	/**
	 * @param processor
	 */
	public ChatMessageHandler(ClientProcessor processor) {
		super(processor);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.topblack.mobile.hineighbor.RequestHandler#handleRequest()
	 */
	@Override
	public int handleRequest(byte[] body) throws Exception {
		Log.d(LOG_TAG, "Handle request " + body.length + " bytes");
		Message message = Message.fromBytes(body);
		if (message == null) {
			return ServiceErrorCode.ServiceRequestParseError;
		}
		
		MessageRouter.getInstance().handleMessage(message);
		
		return ServiceErrorCode.ServiceResponseOK;
	}
}
