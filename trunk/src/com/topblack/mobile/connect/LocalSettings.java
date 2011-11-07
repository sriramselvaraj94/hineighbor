/**
 * File:    AppSettings.java
 * Author : 10115154
 * Created: Nov 5, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.connect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

/**
 * @author 10115154
 * 
 */
public class LocalSettings {

	private final static String LOG_TAG = LocalSettings.class.getSimpleName();

	public static final String SERVICE_TYPE_TITLE_IM = "Instant Message";
	public static final String SERVICE_TYPE_TITLE_VC = "Voice Chat";
	public static final String SERVICE_TYPE_TITLE_FT = "File Transfer";

	public static final String SERVICE_TYPE_ID_IM = "_neighborim._tcp.local.";
	public static final String SERVICE_TYPE_ID_VC = "_neighborvc._tcp.local.";
	public static final String SERVICE_TYPE_ID_FT = "_neighborft._tcp.local.";

	private static Map<String, String> supportedServices = new HashMap<String, String>();
	
	private static String localIdentity = null;

	static {
		supportedServices.put(SERVICE_TYPE_TITLE_IM, SERVICE_TYPE_ID_IM);
		supportedServices.put(SERVICE_TYPE_TITLE_VC, SERVICE_TYPE_ID_VC);
		supportedServices.put(SERVICE_TYPE_TITLE_FT, SERVICE_TYPE_ID_FT);
	}
	
	public static void setLocalId(String localId) {
		localIdentity = localId;
	}
	
	public static String getLocalId() {
		return localIdentity;
	}
	
	public static String[] getSupportedServiceTitles() {
		Set<String> resultSet = LocalSettings.supportedServices.keySet();
		Object[] values = resultSet.toArray();
		String[] result = new String[values.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = values[i].toString();
		}
		
		return result;
	}

	public static List<String> getEnabledServices(Activity act) {
		List<String> result = new LinkedList<String>();
		Log.i(LOG_TAG, act + " get enabled services");
		try {
			SharedPreferences preferences = act.getSharedPreferences(
					"HiNeighbor_Settings", 0);
			for (String serviceTitle : LocalSettings.supportedServices.keySet()) {
				if (preferences.getBoolean(serviceTitle, false)) {
					result.add(serviceTitle);
				}
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
		}

		Log.v(LOG_TAG, result.size() + " services is enabled");
		return result;
	}

	public static String getServiceTypeByTitle(String title) {
		return supportedServices.get(title);
	}

	public static boolean isOptionEnabled(Activity act, String optionName,
			boolean defaultValue) {
		Log.i(LOG_TAG, act + ": Is Option " + optionName + " Enabled?");
		try {
			SharedPreferences preferences = act.getSharedPreferences(
					"HiNeighbor_Settings", 0);
			boolean optionValue = preferences.getBoolean(optionName, false);
			Log.i(LOG_TAG, optionName + ": " + optionValue);
			return optionValue;
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
		}
		return defaultValue;
	}

	public static void enableOption(Activity act, String optionName,
			boolean enable) {
		Log.i(LOG_TAG, act + ": Set Option " + optionName + " to " + enable);
		try {
			SharedPreferences preferences = act.getSharedPreferences(
					"HiNeighbor_Settings", 0);
			Editor editor = preferences.edit();
			editor.putBoolean(optionName, enable);
			editor.commit();
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
		}
	}
}
