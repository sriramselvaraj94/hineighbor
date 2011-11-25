/**
 * File:    ClientProcessorFactory.java
 * Author : 10115154
 * Created: Nov 24, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

/**
 * @author 10115154
 * 
 */
public class ClientProcessorFactory {
	private static final String LOG_TAG = ClientProcessorFactory.class.getSimpleName();
	
	private static ClientProcessorFactory instance = null;

	public static ClientProcessorFactory getInstance() {
		if (null == instance) {
			instance = new ClientProcessorFactory();
		}

		return instance;
	}

	private List<ClientProcessor> processors = new LinkedList<ClientProcessor>();

	public ClientProcessor createProcessor(Socket clientSocket) {
		Log.d(LOG_TAG, "Create processor");
		ClientProcessor processor = new ClientProcessor(clientSocket);
		this.processors.add(processor);
		return processor;
	}
	
	public void destroyProcessor(ClientProcessor processor) {
		Log.d(LOG_TAG, "Destroy processor");
		this.processors.remove(processor);
	}
}
