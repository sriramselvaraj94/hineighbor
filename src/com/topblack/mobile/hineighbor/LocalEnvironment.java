/**
 * File:    AppSettings.java
 * Author : 10115154
 * Created: Nov 5, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * @author 10115154
 * 
 */
public class LocalEnvironment {

	private final static String LOG_TAG = LocalEnvironment.class
			.getSimpleName();

	public static final String SERVICE_TYPE_TITLE_IM = "Instant Message";
	public static final String SERVICE_TYPE_TITLE_VC = "Voice Chat";
	public static final String SERVICE_TYPE_TITLE_FT = "File Transfer";

	public static final String SERVICE_TYPE_ID_IM = "_neighborim._tcp.local.";
	public static final String SERVICE_TYPE_ID_VC = "_neighborvc._tcp.local.";
	public static final String SERVICE_TYPE_ID_FT = "_neighborft._tcp.local.";

	public static final String DEFAULT_NAME = "Neighbor X";

	public static final String PREFERENCE_NAME = "HiNeighbor_Settings";

	private static Map<String, String> supportedServices = new HashMap<String, String>();

	public static String LocalIdentity = null;

	public static int LocalIPAddress = 0;

	public static int LocalIPPort = 0;

	static {
		supportedServices.put(SERVICE_TYPE_TITLE_IM, SERVICE_TYPE_ID_IM);
		supportedServices.put(SERVICE_TYPE_TITLE_VC, SERVICE_TYPE_ID_VC);
		supportedServices.put(SERVICE_TYPE_TITLE_FT, SERVICE_TYPE_ID_FT);
	}
	
	private static List<IEnvironmentChangeListener> listeners = new LinkedList<IEnvironmentChangeListener>();
	
	public static void registerListener(IEnvironmentChangeListener listener) {
		listeners.add(listener);
	}
	
	public static void unregisterListener(IEnvironmentChangeListener listener) {
		listeners.remove(listener);
	}

	public static String[] getSupportedServiceTitles() {
		Set<String> resultSet = LocalEnvironment.supportedServices.keySet();
		Object[] values = resultSet.toArray();
		String[] result = new String[values.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = values[i].toString();
		}

		return result;
	}

	public static List<String> getEnabledServices(Context context) {
		List<String> result = new LinkedList<String>();
		Log.i(LOG_TAG, context + " get enabled services");
		try {
			SharedPreferences preferences = context.getSharedPreferences(
					"HiNeighbor_Settings", 0);
			for (String serviceTitle : LocalEnvironment.supportedServices
					.keySet()) {
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

	public static boolean isOptionEnabled(Context ctx, String optionName,
			boolean defaultValue) {
		Log.i(LOG_TAG, ctx + ": Is Option " + optionName + " Enabled?");
		try {
			SharedPreferences preferences = ctx.getSharedPreferences(
					PREFERENCE_NAME, 0);
			boolean optionValue = preferences.getBoolean(optionName, false);
			Log.i(LOG_TAG, optionName + ": " + optionValue);
			return optionValue;
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
		}
		return defaultValue;
	}

	public static void enableOption(Context ctx, String optionName,
			boolean enable) {
		Log.i(LOG_TAG, ctx + ": Set Option " + optionName + " to " + enable);
		try {
			SharedPreferences preferences = ctx.getSharedPreferences(
					PREFERENCE_NAME, 0);
			Editor editor = preferences.edit();
			editor.putBoolean(optionName, enable);
			editor.commit();
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
		}
	}

	public static String getLocalName(Context ctx) {
		Log.i(LOG_TAG, ctx + ": get local name ...");

		try {
			SharedPreferences preferences = ctx.getSharedPreferences(
					PREFERENCE_NAME, 0);
			return preferences.getString("LocalName", DEFAULT_NAME);
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
			return DEFAULT_NAME;
		}
	}

	public static void setLocalName(Context ctx, String name) {
		Log.i(LOG_TAG, ctx + ": set local name to " + name);
		try {
			if (name != null && name.length() > 0) {
				SharedPreferences preferences = ctx.getSharedPreferences(
						PREFERENCE_NAME, 0);
				Editor editor = preferences.edit();
				editor.putString("LocalName", name);
				editor.commit();
			}
			
			for (IEnvironmentChangeListener listener : listeners) {
				listener.environmentChanged();
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
		}
	}
	
	public static String getFullName(Context ctx) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getLocalName(ctx));
		sb.append("@");
		sb.append(LocalIdentity);
		//TODO replace this hard coded service name
		sb.append("._hineighbor._tcp.local.");
		sb.append("@");
		sb.append(DataConvertUtility.intIPv42String(LocalIPAddress));
		sb.append(":");
		sb.append(LocalIPPort);
		
		return sb.toString();
	}
}
