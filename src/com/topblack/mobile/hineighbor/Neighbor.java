/**
 * File:    Neighbor.java
 * Author : 10115154
 * Created: Nov 24, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import android.util.Log;

/**
 * @author 10115154
 * 
 */
public class Neighbor {
	private static final String LOG_TAG = Neighbor.class.getSimpleName();
	
	private String nickName = null;
	private String identity = null;
	private String address = null;
	private boolean valid = false;

	public Neighbor(String fullName) {
		try {
			String[] subNames = fullName.split("@");
			this.nickName = subNames[0];
			this.identity = subNames[1];
			this.address = subNames[2];
			this.valid = true;
		} catch (Exception ex) {
			Log.w(LOG_TAG, "Invalid neighbor, " + ex);
			this.valid = false;
		}
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identity == null) ? 0 : identity.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Neighbor other = (Neighbor) obj;
		if (identity == null) {
			if (other.identity != null)
				return false;
		} else if (!identity.equals(other.identity))
			return false;
		return true;
	}


	public String toString() {
		return this.nickName + "@" + this.identity + "@" + this.address;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
