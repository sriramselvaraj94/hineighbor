/**
 * File:    MessageHandler.java
 * Author : 10115154
 * Created: Nov 24, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

/**
 * @author 10115154
 * 
 */
public abstract class MessageHandler {
	private final static String LOG_TAG = MessageHandler.class.getSimpleName();

	private String[] acceptedMessageFilter = null;

	private Thread messageProcessThread = null;

	private BlockingQueue<Message> messageQueue = null;
	
	private boolean stopRequested = false;

	public MessageHandler(String[] filter) {
		this.messageQueue = new LinkedBlockingQueue<Message>();
		this.messageProcessThread = new Thread() {
			public void run() {
				while (!stopRequested) {
					try {
						Message message = MessageHandler.this.messageQueue
								.take();
						if (message != null) {
							handleMessage(message);
						}
					} catch (InterruptedException ex) {
						Log.e(LOG_TAG, ex.toString());
					}
				}
			}
		};
	}

	public boolean supportMessage(Message message) {
		if (this.acceptedMessageFilter == null) {
			return true;
		}

		String messageId = message.getMessageId();
		for (String supportedPrefix : this.acceptedMessageFilter) {
			if (messageId.startsWith(supportedPrefix)) {
				return true;
			}
		}

		return false;
	}

	public void start() {
		this.messageProcessThread.start();
	}

	public void stop() {
		stopRequested = true;
		this.messageProcessThread.stop();
	}

	public void putMessage(Message message) {
		try {
			this.messageQueue.put(message);
		} catch (InterruptedException ex) {
			Log.e(LOG_TAG, ex.toString());
		}
	}

	protected abstract void handleMessage(Message message);
}
