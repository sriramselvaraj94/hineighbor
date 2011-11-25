/**
 * File:    ClientProcessor.java
 * Author : 10115154
 * Created: Nov 11, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
		requestHandlers.put("CHAT", ChatMessageHandler.class);
	}

	private Socket clientSocket = null;

	private InputStream is = null;

	private OutputStream os = null;

	public ClientProcessor(Socket clientSocket) {
		this.clientSocket = clientSocket;
		try {
			this.is = clientSocket.getInputStream();
			this.os = clientSocket.getOutputStream();
		} catch (IOException ex) {
			this.shutdown();
		}
	}

	private void respond(int code) throws IOException {
		if (this.os != null) {
			Log.d(LOG_TAG, "Respond " + code);
			this.os.write(code);
		}
	}

	public void shutdown() {
		try {
			if (this.is != null) {
				this.is.close();
			}
			if (this.os != null) {
				this.os.close();
			}
			if (this.clientSocket != null) {
				this.clientSocket.close();
			}

		} catch (IOException innerEx) {
			innerEx.printStackTrace();
			// this exception should be ignored.
		} finally {
			this.is = null;
			this.os = null;
			this.clientSocket = null;
			ClientProcessorFactory.getInstance().destroyProcessor(this);
			Log.d(LOG_TAG, "Client processor is shutdown.");
		}
	}

	public void run() {
		try {
			byte[] headerBuffer = new byte[4];
			int len = this.is.read(headerBuffer);
			if (len <= 0) {
				throw new BadRequestException();
			}
			if (len != headerBuffer.length) {
				throw new BadRequestException();
			}

			String headerString = new String(headerBuffer);
			Log.v(LOG_TAG, "Received " + headerString);

			// TODO Auto-generated method stub
			byte[] lengthBuffer = new byte[4];

			int readLength = this.is.read(lengthBuffer);
			if (readLength < lengthBuffer.length) {
				Log.w(LOG_TAG, "CTRL Packet Abandoned - Invalid Length Field!");
				throw new BadRequestException();
			}

			int bodyLength = DataConvertUtility.bytes2Int(lengthBuffer);
			byte[] controlBuffer = new byte[bodyLength];
			len = this.is.read(controlBuffer);
			if (len < controlBuffer.length) {
				Log.w(LOG_TAG, "CTRL Packet Abandoned - Invalid Control Body!");
				throw new BadRequestException();
			}

			Class handlerClassType = this.requestHandlers.get(headerString);

			Constructor cons = handlerClassType.getConstructor(this.getClass());
			RequestHandler handler = (RequestHandler) cons.newInstance(this);
			this.respond(handler.handleRequest(controlBuffer));

		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.toString());
			try {
				if (ex instanceof BadRequestException) {
					this.respond(ServiceErrorCode.ServiceBadRequest);
				} else {
					this.respond(ServiceErrorCode.ServiceBadRequest);
				}
			} catch (Exception innerEx) {
				Log.e(LOG_TAG, innerEx.toString());
			}
		} finally {
			this.shutdown();
		}
	}
}
