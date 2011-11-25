/**
 * File:    ServiceServer.java
 * Author : 10115154
 * Created: Nov 8, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

//import android.util.Log;

/**
 * @author 10115154
 * 
 */
public class ServiceServer {

	public static void main(String[] args) {
		ServiceServer server = new ServiceServer("Test Server");

		int startPort = server.start();
		System.out.println("Server is started at " + startPort);

		try {
			System.in.read();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private final static String LOG_TAG = ServiceServer.class.getSimpleName();

	// The thread which hosts the socket
	private Thread serverThread = null;

	// The main socket which listen to the local port
	private ServerSocket serverSocket = null;

	private String serverName = null;

	private int serverPort = -1;

	private boolean stopRequested = false;

	public ServiceServer(String serverName) {
		this.serverName = serverName;
		this.serverThread = new Thread() {
			public void run() {
				serverThreadProc();
			}
		};
	}

	private void serverThreadProc() {
		while (!this.stopRequested) {
			Socket sessionSocket = null;
			try {
				sessionSocket = this.serverSocket.accept();
				InetAddress sourceAddress = sessionSocket.getInetAddress();
				Log.i(LOG_TAG, "Accepted new incoming connection ... "
						+ sourceAddress.toString());

				ClientProcessorFactory.getInstance().createProcessor(sessionSocket).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int start() {

		try {
			this.serverSocket = new ServerSocket(0);
			this.serverPort = this.serverSocket.getLocalPort();
			Log.i(LOG_TAG, "Server " + serverName + " started at "
					+ this.serverSocket.getLocalSocketAddress());
		} catch (IOException ex) {
			Log.i(LOG_TAG, ex.toString());
		}

		this.serverThread.start();

		return this.serverPort;
	}

	public void stop() {
		try {
			stopRequested = true;
			if (null != this.serverSocket) {
				this.serverSocket.close();
			}
		} catch (IOException ex) {
			Log.i(LOG_TAG, ex.toString());
		}

		Log.i(LOG_TAG, "The server " + this.serverName + " has been stopped.");
	}
}
