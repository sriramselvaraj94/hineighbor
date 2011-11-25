/**
 * File:    HiNeighborService.java
 * Author : 10115154
 * Created: Nov 15, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
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

	// This list cache the known service info, retrieved from local network.
	private List<ServiceInfo> knownServiceInfos = new LinkedList<ServiceInfo>();

	// Keeps the last activity time of the neighbor. If last activity expired,
	// the entry will be removed.
	private Map<Neighbor, Long> neighborLastActivityTime = new HashMap<Neighbor, Long>();

	private Map<Neighbor, List<Message>> recentMessages = new HashMap<Neighbor, List<Message>>();

	// Defines the timeout value for the communication with the neighbor.
	private static final int ACTIVITY_TIMEOUT = 60 * 1000; // ms

	private ServiceServer server = null;

	private boolean started = false;

	private JmDNS registry = null;

	private final Map<String, com.topblack.mobile.hineighbor.Message> unreadMessages = new HashMap<String, com.topblack.mobile.hineighbor.Message>();

	private List<INotificationListener> notificationListeners = new LinkedList<INotificationListener>();

	private NotificationManager notificationManager = null;

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
	 * @see android.app.Service#onCreate()
	 */
	public void onCreate() {
		try {
			registry = JmDNS.create();
			WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiManager.MulticastLock multicastLock = wm
					.createMulticastLock("mydebuginfo");
			multicastLock.acquire();
		} catch (Exception ex) {
			Log.e(LOG_TAG, "Unable to create mDNS service! " + ex.getMessage());
			return;
		}

		this.chatMessageHandler.start();
		MessageRouter.getInstance().registerMessageHandler(
				this.chatMessageHandler);
		this.server = new ServiceServer("HiNeighbor_Control");

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
		LocalEnvironment.LocalIPPort = startedPort;
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

	private Neighbor getNeighborById(String id) {
		Set<Neighbor> activeNeighbors = this.neighborLastActivityTime.keySet();
		for (Neighbor neighbor : activeNeighbors) {
			if (neighbor.getIdentity().equals(id)) {
				return neighbor;
			}
		}

		return null;
	}

	private List<Neighbor> getActiveNeighbors() {
		resetAvailableServices();

		long currentTime = System.currentTimeMillis();
		List<Neighbor> result = new LinkedList<Neighbor>();
		Set<Neighbor> activeNeighbors = this.neighborLastActivityTime.keySet();
		for (Neighbor neighbor : activeNeighbors) {
			long lastActivityTime = this.neighborLastActivityTime.get(neighbor)
					.longValue();
			if (currentTime - lastActivityTime < ACTIVITY_TIMEOUT) {
				if (neighbor.getIdentity().contains(
						LocalEnvironment.LocalIdentity)) {
					if (!neighbor.getNickName().contains("[ME]")) {
						neighbor.setNickName(neighbor.getNickName() + " [ME]");
					}
				}
				result.add(neighbor);
			} else {
				Log.d(LOG_TAG, neighbor
						+ " becomes inactive. Last activity time occurs at "
						+ new Date(lastActivityTime));
			}
		}

		return result;
	}

	/**
	 * Threadsafe function to update the service cache.
	 * 
	 * @param info
	 *            the service info to update.
	 * @param isAdd
	 *            indicate if this service is to add or remove
	 */
	private synchronized void updateAvailableService(ServiceInfo info,
			boolean isAdd) {
		Log.i(LOG_TAG, "Updating available services... isAdd?" + isAdd
				+ ", info?" + info);
		if (info == null) {
			return;
		}

		long currentTime = System.currentTimeMillis();
		if (isAdd) {
			if (this.knownServiceInfos.contains(info)) {
				this.knownServiceInfos.remove(info);
			}
			this.knownServiceInfos.add(info);

			Neighbor neighbor = DataConvertUtility.toNeighborInfo(info);
			if (neighbor.isValid()) {
				this.neighborLastActivityTime.put(neighbor,
						System.currentTimeMillis());
			}
			Log.d(LOG_TAG, "Update neighbor " + neighbor);
		} else {
			this.knownServiceInfos.remove(info);
		}

		List<Neighbor> inactiveNeighbors = new LinkedList<Neighbor>();
		Set<Neighbor> activeNeighbors = this.neighborLastActivityTime.keySet();
		for (Neighbor neighbor : activeNeighbors) {
			if (currentTime
					- this.neighborLastActivityTime.get(neighbor).longValue() > ACTIVITY_TIMEOUT) {
				inactiveNeighbors.add(neighbor);
			}
		}

		for (Neighbor neighbor : inactiveNeighbors) {
			Log.i(LOG_TAG, neighbor + " becomes inactive!");
			this.neighborLastActivityTime.remove(neighbor);
		}

		Log.v(LOG_TAG, this.knownServiceInfos.size()
				+ " services are available.");
	}

	/**
	 * Query the available services in the local network. (Async) And update the
	 * local neighbor activity map.
	 */
	private void resetAvailableServices() {
		Log.i(LOG_TAG, "Reset available services...");
		new Thread() {
			public void run() {
				try {
					ServiceInfo[] sinfos = registry.list(localServiceInfo
							.getType());
					Log.v(LOG_TAG, "Retrieved " + sinfos.length + " services.");
					HiNeighborService.this.notifyAll("TestNotificationId");
					for (ServiceInfo info : sinfos) {
						Log.i(LOG_TAG, "Got service " + info.toString());
						if (info.getName().equalsIgnoreCase(
								LocalEnvironment.LocalIdentity)) {
							Log.d(LOG_TAG, "Detected local service.");
						}

						updateAvailableService(info, true);
					}
				} catch (Exception ex) {
					Log.i(LOG_TAG, "Failed to retrieve available services. "
							+ ex.toString());
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
			this.chatMessageHandler.stop();
			MessageRouter.getInstance().unregisterMessageHandler(
					this.chatMessageHandler);

			if (this.registry != null) {
				this.registry.removeServiceListener(
						this.localServiceInfo.getType(), this.serviceListener);
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

	private void notifyAll(String notificationId) {
		Log.i(LOG_TAG, "Notify all, " + notificationId);
		for (INotificationListener listener : this.notificationListeners) {
			Log.d(LOG_TAG, "Notify listener " + listener + " ...");
			listener.notificationReceived(notificationId);
		}
	}

	private static final int NotificationId = 20111124;

	private void notifyUser(String title, String content) {
		this.notificationManager.cancel(NotificationId);
		Notification notification = new Notification();

		notification.icon = R.drawable.ic_launcher;
		Intent notificationIntent = new Intent(this, ConnectActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.tickerText = "Hi, Neighbor!";
		notification.setLatestEventInfo(this, title, content, contentIntent);

		notificationManager.notify(NotificationId, notification);
	}

	private IBinder serviceBinder = new ServiceBinder();

	private MessageHandler chatMessageHandler = new ChatMessageHandler();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, intent + " is bond to the service");
		return serviceBinder;
	}

	public class ChatMessageHandler extends MessageHandler {

		public ChatMessageHandler() {
			super(new String[] { "TXTCHAT" });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.topblack.mobile.hineighbor.MessageHandler#handleMessage(com.topblack
		 * .mobile.hineighbor.Message)
		 */
		@Override
		protected void handleMessage(
				com.topblack.mobile.hineighbor.Message message) {
			// TODO Auto-generated method stub
			Log.i(LOG_TAG,
					"ChatMessageHandler:Handle message - " + message.toString());
			unreadMessages.put(message.getMessageId(), message);

			if (!recentMessages.containsKey(message.getSourceId())) {
				List<Message> messageList = new LinkedList<Message>();
				recentMessages.put(message.getSourceId(), messageList);
			}

			HiNeighborService.this.neighborLastActivityTime.put(
					message.getSourceId(), System.currentTimeMillis());

			notifyUser("Hi, Neighbor!", message.getSourceId().getNickName()
					+ ":" + message.getMessageContent());
			HiNeighborService.this.notifyAll(message.getMessageId());
		}

	}

	public class ServiceBinder extends Binder implements IHiNeighborService {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.topblack.mobile.hineighbor.IHiNeighborService#getAvailableServices
		 * ()
		 */
		@Override
		public List<Neighbor> getActiveNeighbors() {
			// TODO Auto-generated method stub
			return HiNeighborService.this.getActiveNeighbors();
		}

		private int sendMessage(Message message) {
			Socket socket = null;
			InputStream is = null;
			OutputStream os = null;
			try {
				String ipAddressWithPort = message.getTargetId().getAddress();
				String[] ipSubAddresses = ipAddressWithPort.split(":");
				String ipAddress = ipSubAddresses[0];
				int ipPort = Integer.parseInt(ipSubAddresses[1]);
				socket = new Socket(ipAddress, ipPort);
				is = socket.getInputStream();
				os = socket.getOutputStream();

				byte[] header = "CHAT".getBytes();
				byte[] body = message.toBytes();
				os.write(header);
				os.write(DataConvertUtility.int2Bytes(body.length));
				os.write(body);

				return is.read();
			} catch (Exception ex) {
				Log.i(LOG_TAG, "Send message failed!" + ex);
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (Exception innerEx) {
					// Ignore
				}
				try {
					if (os != null) {
						os.close();
					}
				} catch (Exception innerEx) {
					// Ignore
				}
				try {
					if (socket != null) {
						socket.close();
					}
				} catch (Exception innerEx) {
					// Ignore
				}
			}
			return -1;
		}

		public Message postMessage(String targetId, String messageContent) {
			Neighbor targetNeighbor = getNeighborById(targetId);

			if (null == targetNeighbor || messageContent == null) {
				return null;
			}

			String localName = LocalEnvironment
					.getFullName(HiNeighborService.this);
			String targetName = targetNeighbor.getNickName() + "@"
					+ targetNeighbor.getIdentity() + "@"
					+ targetNeighbor.getAddress();

			Message message = new Message("TXTCHAT", localName, targetName,
					messageContent);
			int response = this.sendMessage(message);
			Log.i(LOG_TAG, "Send message response:" + response);

			return message;
		}

		public com.topblack.mobile.hineighbor.Message getMessage(
				String messageId, boolean markAsRead) {
			Message message = unreadMessages.get(messageId);
			if (markAsRead) {
				unreadMessages.remove(messageId);
			}

			return message;
		}

		public List<Message> getRecentMessages(String sourceId) {
			List<Message> result = new LinkedList<Message>();

			return result;
		}

		public List<com.topblack.mobile.hineighbor.Message> getUnreadMessages(
				String sourceId, boolean markAsRead) {
			List<com.topblack.mobile.hineighbor.Message> result = new LinkedList<com.topblack.mobile.hineighbor.Message>();
			for (Message message : unreadMessages.values()) {
				if (message.getSourceId().getIdentity().equals(sourceId)) {
					result.add(message);
					if (markAsRead) {
						unreadMessages.remove(message.getMessageId());
					}
				}
			}
			return result;
		}

		public void registerListener(INotificationListener listener) {
			Log.i(LOG_TAG, "Register listener " + listener + " ...");
			notificationListeners.add(listener);
		}

		public void unregisterListener(INotificationListener listener) {
			notificationListeners.remove(listener);
		}
	}

}
