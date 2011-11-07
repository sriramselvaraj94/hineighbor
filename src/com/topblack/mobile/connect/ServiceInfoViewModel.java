/**
 * File:    ServiceInfoViewModel.java
 * Author : 10115154
 * Created: Nov 1, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.connect;

import java.util.HashMap;

/**
 * @author 10115154
 * 
 */
public class ServiceInfoViewModel extends HashMap<String, String> {

	public String toString() {
		return this.getServiceName() != null ? this.getServiceName() : "";
	}

	public ServiceInfoViewModel(String serviceName, String serviceIp) {
		this.setServiceName(serviceName);
		this.setServiceIp(serviceIp);
		this.setServiceDesc(serviceIp);
	}

	public ServiceInfoViewModel(String serviceName, String serviceIp,
			String serviceDesc) {
		this.setServiceName(serviceName);
		this.setServiceIp(serviceIp);
		this.setServiceDesc(serviceDesc);
	}

	public String getServiceName() {
		return this.get("serviceName");

	}

	public void setServiceName(String serviceName) {
		this.put("serviceName", serviceName);
	}

	public String getServiceDesc() {
		return this.get("serviceDesc");
	}

	public void setServiceDesc(String serviceDesc) {
		this.put("serviceDesc", serviceDesc);
	}

	public void setServiceIp(String ipAddress) {
		this.put("serviceIp", ipAddress);
	}

	public String getServiceIp() {
		return this.get("serviceIp");
	}

}
