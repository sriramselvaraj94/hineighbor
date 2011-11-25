/**
 * File:    DataConvertUtility.java
 * Author : 10115154
 * Created: Nov 11, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.net.Inet4Address;
import java.text.SimpleDateFormat;

import javax.jmdns.ServiceInfo;

/**
 * @author 10115154
 * 
 */
public class DataConvertUtility {
	public static final String CHAT_TIME_FORMAT = "HH:mm:ss";
	
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat(CHAT_TIME_FORMAT);
	
	public static String intIPv42String(int value) {
		byte[] addressBytes = int2Bytes(value);
		String addressString = null;
		for (byte subAddress : addressBytes) {
			if (null == addressString) {
				addressString = "" + (0x00FF & subAddress);
			} else {
				addressString = addressString + "." + (0x00FF & subAddress);
			}
		}
		return addressString;
	}
	
	public static byte[] int2Bytes(int intValue) {
		byte[] result = new byte[4];

		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) (intValue >> i * 8);
		}

		return result;
	}

	public static int bytes2Int(byte[] buffer) {
		int result = 0;
		for (int i = 0; i < buffer.length; i++) {
			result |= ((0x000000FF & buffer[i]) << i * 8);
		}
		return result;
	}

	public static Neighbor toNeighborInfo(ServiceInfo info) {
		String serviceName = info.getQualifiedName();
		Inet4Address[] addresses = info.getInet4Addresses();
		int servicePort = info.getPort();
		String addressString = null;
		for (Inet4Address address : addresses) {
			byte[] addressBytes = address.getAddress();
			for (byte subAddress : addressBytes) {
				if (null == addressString) {
					addressString = "" + (0x00FF & subAddress);
				} else {
					addressString = addressString + "." + (0x00FF & subAddress);
				}
			}

			break;
		}
		String nickName = new String(info.getPropertyBytes("NeighborName"));

		String fullName = nickName + "@" + serviceName + "@" + addressString
				+ ":" + servicePort;
		return new Neighbor(fullName);
	}
}
