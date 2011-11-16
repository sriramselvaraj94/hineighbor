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
public class ControlRequestHandler extends RequestHandler {

	/**
	 * @param processor
	 */
	public ControlRequestHandler(ClientProcessor processor) {
		super(processor);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.topblack.mobile.hineighbor.RequestHandler#handleRequest()
	 */
	@Override
	public void handleRequest() throws Exception{
		// TODO Auto-generated method stub
		byte[] lengthBuffer = new byte[4];

		int len = this.processor.read(lengthBuffer);
		if (len < lengthBuffer.length) {
			Log.w(LOG_TAG, "CTRL Packet Abandoned - Invalid Length Field!");
			throw new InvalidPacketException(
					"CTRL Packet Abandoned - Invalid Length Field!");
		}

		int bodyLength = DataConvertUtility.bytes2Int(lengthBuffer);
		byte[] controlBuffer = new byte[bodyLength];
		len = this.processor.read(controlBuffer);
		if (len < controlBuffer.length) {
			Log.w(LOG_TAG, "CTRL Packet Abandoned - Invalid Control Body!");
			throw new InvalidPacketException(
					"CTRL Packet Abandoned - Invalid Length Field!");
		}

		String controlContent = new String(controlBuffer);
		String[] controlSections = controlContent.split("#");
		for (String controlSection : controlSections) {
			Log.v(LOG_TAG, controlSection);
		}
		
		if (controlSections[0].equalsIgnoreCase("TEXT")) {
			handleTextCommands(controlSections);
		}
	}
	
	private void handleTextCommands(String[] commandSections) {
		
	}

}
