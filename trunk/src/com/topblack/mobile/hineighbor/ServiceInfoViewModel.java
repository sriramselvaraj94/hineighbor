/**
 * File:    ServiceInfoViewModel.java
 * Author : 10115154
 * Created: Nov 1, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.util.HashMap;

import javax.jmdns.ServiceInfo;

import android.util.Log;

/**
 * @author 10115154
 * 
 */
public class ServiceInfoViewModel extends HashMap<String, String> {

	private final static String LOG_TAG = ServiceInfoViewModel.class
			.getSimpleName();

	private String qualifiedName = null;

	@Override
	public String toString() {
		return this.getServiceName() != null ? this.getServiceName() : "";
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (null == this.qualifiedName) {
			return false;
		}
		return qualifiedName.equals(obj);
	}

	public ServiceInfoViewModel(ServiceInfo serviceInfo) {
		this.qualifiedName = serviceInfo.getQualifiedName();
		Log.i(LOG_TAG, "Qualified name:" + this.qualifiedName);
		String[] qualifiedNames = this.qualifiedName.split("\\.");
		if (qualifiedNames.length > 0) {
			this.setServiceName(qualifiedNames[0]);
			this.setServiceDesc(serviceInfo.getQualifiedName());
			Log.d(LOG_TAG, "Service Name:" + this.getServiceName());
		}
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
