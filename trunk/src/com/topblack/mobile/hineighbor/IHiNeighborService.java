/**
 * File:    IHiNeighborService.java
 * Author : 10115154
 * Created: Nov 21, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.util.List;

import javax.jmdns.ServiceInfo;

/**
 * @author 10115154
 *
 */
public interface IHiNeighborService {
	List<ServiceInfo> getAvailableServices();
	
	void postMessage(String targetId, String message);
	
	Message getMessage(String messageId, boolean markAsRead);
	
	List<Message> getUnreadMessages(String sourceId, boolean markAsRead);
	
	void registerListener(INotificationListener listener);
	
	void unregisterListener(INotificationListener listener);
}
