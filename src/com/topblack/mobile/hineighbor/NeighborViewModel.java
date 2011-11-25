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
public class NeighborViewModel extends HashMap<String, String> {

	private final static String LOG_TAG = NeighborViewModel.class
			.getSimpleName();

	private String shortName = null;
	
	private String qualifiedName = null;

	@Override
	public String toString() {
		return this.getNeighborName() != null ? this.getNeighborDesc() : "";
	}


	@Override
	public int hashCode() {
		return 0;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		NeighborViewModel other = (NeighborViewModel) obj;
		if (qualifiedName == null) {
			if (other.qualifiedName != null)
				return false;
		} else if (!qualifiedName.equals(other.qualifiedName))
			return false;
		return true;
	}


	public NeighborViewModel(Neighbor neighbor) {
		this.qualifiedName = neighbor.getIdentity();
		this.shortName = neighbor.getNickName();
		Log.i(LOG_TAG, "Qualified name:" + this.qualifiedName);

		this.setNeighborAddress(neighbor.getAddress());
		this.setNeighborName(this.shortName);
		this.setNeighborDesc(this.qualifiedName);
	}

	public String getNeighborName() {
		return this.get("neighborName");

	}

	public void setNeighborName(String serviceName) {
		this.put("neighborName", serviceName);
	}

	public String getNeighborDesc() {
		return this.get("neighborDesc");
	}

	public void setNeighborDesc(String serviceDesc) {
		this.put("neighborDesc", serviceDesc);
	}
	
	public String getNeighborAddress() {
		return this.get("neighborAddr");
	}
	
	public void setNeighborAddress(String addr) {
		this.put("neighborAddr", addr);
	}

	public void setNeighborUnreadCount(int count) {
		this.put("unreadCount", Integer.toString(count));
		
		if (count > 0) {
			this.setNeighborName(this.shortName + " [" + count + "]");
		} else {
			this.setNeighborName(this.shortName);
		}
	}

	public int getNeighborUnreadCount() {
		return Integer.parseInt(this.get("unreadCount"));
	}

}
