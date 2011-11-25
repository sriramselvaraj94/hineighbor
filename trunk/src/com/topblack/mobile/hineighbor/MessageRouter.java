/**
 * File:    MessageRouter.java
 * Author : 10115154
 * Created: Nov 24, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author 10115154
 *
 */
public class MessageRouter {

	private static MessageRouter instance = null;
	
	public static MessageRouter getInstance() {
		if (null == instance) {
			instance = new MessageRouter();
		}
		
		return instance;
	}
	
	private MessageRouter() {
		
	}
	
	private List<MessageHandler> messageHandlers = new LinkedList<MessageHandler>();
	
	public void registerMessageHandler(MessageHandler handler){
		this.messageHandlers.add(handler);
	}
	
	public void unregisterMessageHandler(MessageHandler handler) {
		this.messageHandlers.remove(handler);
	}
	
	public void handleMessage(Message message) {
		for (MessageHandler handle : messageHandlers) {
			if (handle.supportMessage(message)) {
				handle.putMessage(message);
			}
		}
	}
}
