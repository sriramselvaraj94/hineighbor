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
	
	ISession getSession(ServiceInfo target, SessionInfo session);
}
