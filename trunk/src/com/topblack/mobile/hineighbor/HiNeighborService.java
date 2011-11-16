/**
 * File:    HiNeighborService.java
 * Author : 10115154
 * Created: Nov 15, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author 10115154
 *
 */
public class HiNeighborService extends Service {

	private final static String LOG_TAG = HiNeighborService.class
	.getSimpleName();
	
	public static void startService(Context context) {
		Log.i(LOG_TAG, "Starting HiNeighborService...");
		Intent intent = new Intent();
		intent.setClass(context, HiNeighborService.class);
		context.startService(intent);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		
		
		return null;
	}
	
	public void onCreate() {
		
		super.onCreate();
		Log.i(LOG_TAG, "HiNeighborService is created!");
	}
	
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i(LOG_TAG, "HiNeighborService is started!");
	}

	public void onDestroy() {
		super.onDestroy();
		Log.i(LOG_TAG, "HiNeighborService is destroyed!");
	}
	
	
}
