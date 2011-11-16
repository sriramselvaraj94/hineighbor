/**
 * File:    DataConvertUtility.java
 * Author : 10115154
 * Created: Nov 11, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

/**
 * @author 10115154
 * 
 */
public class DataConvertUtility {
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
}
