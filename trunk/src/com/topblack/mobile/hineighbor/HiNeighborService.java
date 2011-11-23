/**
 * File:    HiNeighborService.java
 * Author : 10115154
 * Created: Nov 15, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * @author 10115154
 * 
 */
public class HiNeighborService extends Service {

	private final static String LOG_TAG = HiNeighborService.class
			.getSimpleName();

	private static final String SERVICE_TYPE = "_hineighbor._tcp.local.";

	private ServiceInfo localServiceInfo = null;

	private List<ServiceInfo> availableServiceInfos = new LinkedList<ServiceInfo>();

	private ServiceServer server = null;
	
	private boolean started = false;
	
	private Handler clientMessageHandler = null;

	private JmDNS registry = null;

	public static void startService(Context context) {
		Log.i(LOG_TAG, "Starting HiNeighborService...");
		Intent intent = new Intent();
		intent.setClass(context, HiNeighborService.class);
		context.startService(intent);
	}

	private boolean checkWifiStatus() {
		try {
			ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); // Network
			State wifiState = conMan.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).getState();

			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();

			LocalEnvironment.LocalIPAddress = info.getIpAddress();
			LocalEnvironment.LocalIdentity = info.getMacAddress();

			Log.i(LOG_TAG, "IP Address:" + LocalEnvironment.LocalIPAddress);
			Log.i(LOG_TAG, "MAC Address:" + LocalEnvironment.LocalIdentity);

			return wifiState == State.CONNECTED;
		} catch (Exception ex) {
			Log.e(this.getClass().getName(), "Check Wifi Status Failed!", ex);
			return false;
		}
	}
	
	/*
	 * 
	 * byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();
			InetAddress address = InetAddress.getByAddress(bytes);
	 * (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */

	public void onCreate() {
		try {
			registry = JmDNS.create();
			WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE); 
			WifiManager.MulticastLock multicastLock = wm.createMulticastLock("mydebuginfo");
			multicastLock.acquire();
		} catch (Exception ex) {
			Log.e(LOG_TAG, "Unable to create mDNS service! " + ex.getMessage());
			return;
		}

		this.server = new ServiceServer("HiNeighbor_Control");
		
		this.clientMessageHandler = new Handler() {
			public void handleMessage(Message msg) {
				Log.i(LOG_TAG, msg.toString());
			}
		};

		super.onCreate();
		Log.i(LOG_TAG, "HiNeighborService is created!");
	}

	private void handleServiceAdded(ServiceEvent event) {
		Log.i(LOG_TAG, "Processing service added event, " + event.toString());
		updateAvailableService(event.getInfo(), true);
	}

	private void handleServiceRemoved(ServiceEvent event) {
		Log.i(LOG_TAG, "Processing service removed event, " + event.toString());
		updateAvailableService(event.getInfo(), false);
	}

	private void handleServiceResolved(ServiceEvent event) {
		Log.i(LOG_TAG, "Processing service resolved event, " + event.toString());
		updateAvailableService(event.getInfo(), true);
	}

	private void startLocalService() {
		int startedPort = server.start();
		Log.i(LOG_TAG, "Started HiNeighbor_Control server at " + startedPort);

		String localName = LocalEnvironment.getLocalName(this);
		String localIdentify = LocalEnvironment.LocalIdentity;
		Map<String, byte[]> properties = new HashMap<String, byte[]>();
		properties.put("NeighborName", localName.getBytes());
		Log.i(LOG_TAG, "Local Identify:" + localIdentify);
		ServiceInfo service = ServiceInfo.create(SERVICE_TYPE, localIdentify,
				startedPort, 0, 0, true, properties);
		
		registry.addServiceListener(service.getType(), serviceListener);
		
		try {
			Log.i(LOG_TAG, "Registering local service " + service);
			registry.registerService(service);
			
			localServiceInfo = service;
		} catch (Exception ex) {
			Log.i(LOG_TAG, "Failed to register service " + localIdentify + ". "
					+ ex.getMessage());
		}
	}
	
	private ServiceListener serviceListener = new ServiceListener() {
		@Override
		public void serviceAdded(ServiceEvent event) {
			Log.d(LOG_TAG, "Service added, " + event);
			HiNeighborService.this.handleServiceAdded(event);
		}

		@Override
		public void serviceRemoved(ServiceEvent event) {
			Log.d(LOG_TAG, "Service removed, " + event);
			HiNeighborService.this.handleServiceRemoved(event);
		}

		@Override
		public void serviceResolved(ServiceEvent event) {
			Log.d(LOG_TAG, "Service resolved, " + event);
			HiNeighborService.this.handleServiceResolved(event);
		}
	};

	/**
	 * Threadsafe function to update the service cache.
	 * @param info the service info to update.
	 * @param isAdd indicate if this service is to add or remove
	 */
	private synchronized void updateAvailableService(ServiceInfo info, boolean isAdd) {
		Log.i(LOG_TAG, "Updating available services... isAdd?" + isAdd + ", info?" + info);
		if (info == null) {
			return;
		}
		if (isAdd) {
			if (this.availableServiceInfos.contains(info)) {
				this.availableServiceInfos.remove(info);
			}
			this.availableServiceInfos.add(info);
		} else {
			this.availableServiceInfos.remove(info);
		}
		
		Log.v(LOG_TAG, this.availableServiceInfos.size() + " services are available.");
	}
	
	/**
	 * Query the available services in the local network. (Async)
	 */
	private void resetAvailableServices() {
		Log.i(LOG_TAG, "Reset available services...");
		new Thread() {
			public void run() {
				try {
					ServiceInfo[] sinfos = registry.list(localServiceInfo.getType());
					Log.v(LOG_TAG, "Retrieved " + sinfos.length + " services.");
					for (ServiceInfo info : sinfos) {
						Log.i(LOG_TAG, "Got service " + info.toString());
						if (info.getName().equalsIgnoreCase(LocalEnvironment.LocalIdentity)) {
							Log.d(LOG_TAG, "Detected local service.");
							continue;
						}
						
						updateAvailableService(info, true);
					}
				} catch (Exception ex) {
					Log.i(LOG_TAG, "Failed to retrieve available services. "
							+ ex.getMessage());
				}
			}
		}.start();
	}

	public void onStart(Intent intent, int startId) {
		if (!checkWifiStatus()) {
			Log.e(LOG_TAG, "Wifi connection is not available.");
			return;
		}
		
		if (!this.started) {
			this.startLocalService();
			this.resetAvailableServices();
			this.started = true;
		}

		super.onStart(intent, startId);
		Log.i(LOG_TAG, "HiNeighborService is started!");
	}

	public void onDestroy() {
		try {
			if (this.registry != null) {
				this.registry.removeServiceListener(this.localServiceInfo.getType(), this.serviceListener);
				this.registry.unregisterAllServices();
				this.registry.close();
			}
			
			if (this.server != null) {
				this.server.stop();
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
		}

		super.onDestroy();
		Log.i(LOG_TAG, "HiNeighborService is destroyed!");
	}
	
	private IBinder serviceBinder = new ServiceBinder();

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, intent + " is bond to the service");
		return serviceBinder;
	}

	public class ServiceBinder extends Binder implements IHiNeighborService{

		/* (non-Javadoc)
		 * @see com.topblack.mobile.hineighbor.IHiNeighborService#getAvailableServices()
		 */
		@Override
		public List<ServiceInfo> getAvailableServices() {
			// TODO Auto-generated method stub
			resetAvailableServices();
			return availableServiceInfos;
		}

		/* (non-Javadoc)
		 * @see com.topblack.mobile.hineighbor.IHiNeighborService#getSession(javax.jmdns.ServiceInfo, com.topblack.mobile.hineighbor.SessionInfo)
		 */
		@Override
		public ISession getSession(ServiceInfo target, SessionInfo session) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
