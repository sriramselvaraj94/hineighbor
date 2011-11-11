/**
 * File:    ClientProcessor.java
 * Author : 10115154
 * Created: Nov 11, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * @author 10115154
 * 
 */
public class ClientProcessor extends Thread {

	private final static String LOG_TAG = ServiceServer.class.getSimpleName();

	private static Map<String, Class> requestHandlers = new HashMap<String, Class>();

	static {
		requestHandlers.put("CTRL", ControlRequestHandler.class);
	}

	private Socket clientSocket = null;

	private InputStream is = null;

	private OutputStream os = null;

	private boolean stopRequested = false;

	public ClientProcessor(Socket clientSocket) {
		this.clientSocket = clientSocket;
		try {
			this.is = clientSocket.getInputStream();
			this.os = clientSocket.getOutputStream();
		} catch (IOException ex) {
			this.shutdown();
		}
	}

	public void write(byte[] buffer) throws IOException {
		if (this.os != null) {
			this.os.write(buffer);
		}
	}

	public int read(byte[] buffer) throws IOException {
		if (this.is != null) {
			return this.is.read(buffer);
		}
		return -1;
	}

	public void shutdown() {
		try {
			stopRequested = true;

			if (this.is != null) {
				this.is.close();
			}
			if (this.os != null) {
				this.os.close();
			}
			this.clientSocket.close();
			this.is = null;
			this.os = null;
			this.clientSocket = null;
		} catch (IOException innerEx) {
			innerEx.printStackTrace();
			// this exception should be ignored.
		}
	}

	public void run() {
		byte[] headerBuffer = new byte[4];
		byte[] lengthBuffer = new byte[4];
		byte[] contentBuffer = new byte[16 * 1024];
		while (!stopRequested) {
			try {
				int len = this.is.read(headerBuffer);
				if (len <= 0) {
					break;
				}
				if (len != headerBuffer.length) {
					Log.w(LOG_TAG, "Invalid header");
					continue;
				}

				String headerString = new String(headerBuffer);
				Log.v(LOG_TAG, "Received " + headerString);

				Class handlerClassType = this.requestHandlers.get(headerString);
				try {
					Constructor cons = handlerClassType.getConstructor(this.getClass());
					RequestHandler handler = (RequestHandler)cons.newInstance(this);
					handler.handleRequest();
				} catch (Exception ex) {
					Log.e(LOG_TAG, ex.toString());
					break;
				}
				this.os.write("Done".getBytes());
			} catch (IOException ex) {
				ex.printStackTrace();
				stopRequested = true;
			}
		}
	}
}
