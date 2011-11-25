package com.topblack.mobile.hineighbor;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The main activity, which list the available services in the local network.
 * 
 * @author 10115154
 */
public class ConnectActivity extends Activity implements INotificationListener,
		IEnvironmentChangeListener {

	private final static String LOG_TAG = ConnectActivity.class.getSimpleName();

	// ===================View Models===================
	// The view model of the service info list
	private List<NeighborViewModel> activeNeighborsViewModel = new ArrayList<NeighborViewModel>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		LocalEnvironment.registerListener(this);

		this.applyNewSettings();

		this.initViewModels();
		this.initEventHandlers();

		this.rebindService();

	}

	private void rebindService() {
		Intent intent = new Intent();
		intent.setClass(this, HiNeighborService.class);
		this.stopService(intent);
		this.startService(intent);
		this.bindService(intent, this.serviceConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		try {
			hiNeighborService.unregisterListener(this);
			this.unbindService(this.serviceConnection);

		} catch (Exception ex) {
			Log.e(LOG_TAG, "Exception occur during destroying the app.");
		}

		super.onDestroy();
	}

	private void refreshServices() {
		Log.i(LOG_TAG, "Refresh available neighbors...");
		final List<Neighbor> activeNeighbors = this.hiNeighborService
				.getActiveNeighbors();
		Log.d(LOG_TAG, activeNeighbors.size() + " active neighbors are found!");

		new Thread(new Runnable() {
			public void run() {
				Log.i(LOG_TAG, "refresh UI...");
				try {

					synchronized (activeNeighborsViewModel) {
						activeNeighborsViewModel.clear();
						for (Neighbor neighbor : activeNeighbors) {
							NeighborViewModel vm = new NeighborViewModel(
									neighbor);
							vm.setNeighborUnreadCount(ConnectActivity.this
									.getUnreadMessageCount(neighbor));
							if (activeNeighborsViewModel.contains(vm)) {
								activeNeighborsViewModel.remove(vm);
							}

							activeNeighborsViewModel.add(vm);
						}
					}
					notifyServiceListChanged();

					if (activeNeighborsViewModel.size() == 0) {
						rebindService();
					}

					Log.i(LOG_TAG, "refresh completed!");
				} catch (Exception ex) {
					ex.printStackTrace();
					Log.e(LOG_TAG, ex.toString());
				}
			}
		}).start();
	}

	private int getUnreadMessageCount(Neighbor target) {
		Log.d(LOG_TAG, this.hiNeighborService + " get unread message count of "
				+ target);
		try {
			return this.hiNeighborService.getUnreadMessages(
					target.getIdentity(), false).size();
		} catch (Exception ex) {
			Log.d(LOG_TAG, ex.toString());
			return 0;
		}
	}

	private void onRefreshButtonClicked(View source) {
		this.refreshServices();
	}

	private void applyNewSettings() {
		TextView localNameLabel = (TextView) this.findViewById(R.id.localName);
		localNameLabel.setText(LocalEnvironment.getLocalName(this));
	}

	private void onSettingsButtonClicked(View source) {
		Intent intent = new Intent();
		intent.setClass(this, SettingsActivity.class);
		this.startActivityForResult(intent, RESULT_OK);
	}

	private void onServiceListItemSelected(AdapterView<?> arg0, View arg1,
			int arg2, long arg3) {
		NeighborViewModel selectedNeighbor = activeNeighborsViewModel.get(arg2);
		selectedNeighbor.setNeighborUnreadCount(0);
		Log.i(LOG_TAG, "Selected neighbor" + selectedNeighbor.getNeighborDesc());
		Intent intent = new Intent();
		intent.putExtra("NeighborName", selectedNeighbor.getNeighborName());
		intent.putExtra("NeighborAddress",
				selectedNeighbor.getNeighborAddress());
		intent.putExtra("NeighborIdentity", selectedNeighbor.getNeighborDesc());
		intent.setClass(this, ChatActivity.class);
		this.startActivityForResult(intent, 1);
		this.notifyServiceListChanged();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(LOG_TAG, "Got activity result. Requested:" + requestCode);
		switch (resultCode) {
		case 1:
			this.applyNewSettings();
			break;
		default:
			Log.w(LOG_TAG, "Activity error!" + resultCode);
			break;
		}
	}

	private void initViewModels() {
		ListView servicesListView = (ListView) this
				.findViewById(R.id.servicesListView);
		ListAdapter servicesListAdapter = new SimpleAdapter(this,
				this.activeNeighborsViewModel,
				android.R.layout.simple_list_item_2, new String[] {
						"neighborName", "neighborDesc" }, new int[] {
						android.R.id.text1, android.R.id.text2 });
		servicesListView.setAdapter(servicesListAdapter);
	}

	/**
	 * Initialize the event handlers
	 */
	private void initEventHandlers() {
		// Service List View
		ListView servicesListView = (ListView) this
				.findViewById(R.id.servicesListView);
		servicesListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						onServiceListItemSelected(arg0, arg1, arg2, arg3);
					}
				});

		// Refresh Button
		Button refreshButton = (Button) this
				.findViewById(R.id.refreshServiceButton);
		refreshButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View source) {
				onRefreshButtonClicked(source);
			}
		});

		// Settings Button
		Button settingsButton = (Button) this
				.findViewById(R.id.appSettingsButton);
		settingsButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View source) {
				onSettingsButtonClicked(source);
			}
		});
	}

	private void notifyServiceListChanged() {
		ConnectActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				ListView servicesListView = (ListView) ConnectActivity.this
						.findViewById(R.id.servicesListView);
				((SimpleAdapter) servicesListView.getAdapter())
						.notifyDataSetChanged();
			}
		});
	}

	private IHiNeighborService hiNeighborService = null;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			hiNeighborService = (IHiNeighborService) service;
			hiNeighborService.registerListener(ConnectActivity.this);
			Log.v(LOG_TAG, "on service connected.");

			refreshServices();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			hiNeighborService = null;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.topblack.mobile.hineighbor.INotificationListener#notificationReceived
	 * (java.lang.String)
	 */
	@Override
	public void notificationReceived(String notificationId) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "Received notification " + notificationId);
		Message message = hiNeighborService.getMessage(notificationId, false);
		if (message == null) {
			Log.w(LOG_TAG, "No message is got.");
			return;
		}
		Neighbor messageSource = message.getSourceId();

		synchronized (activeNeighborsViewModel) {
			NeighborViewModel vm = new NeighborViewModel(messageSource);
			if (!activeNeighborsViewModel.contains(vm)) {
				activeNeighborsViewModel.add(vm);
			}

			int neighborIndex = activeNeighborsViewModel.indexOf(vm);
			if (neighborIndex < 0) {
				Log.v(LOG_TAG, "Unable to find the specific neighbor at "
						+ neighborIndex);
				return;
			}
			List<Message> messages = hiNeighborService.getUnreadMessages(
					messageSource.getIdentity(), false);
			activeNeighborsViewModel.get(neighborIndex).setNeighborUnreadCount(
					messages.size());
			this.notifyServiceListChanged();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.topblack.mobile.hineighbor.IEnvironmentChangeListener#environmentChanged
	 * ()
	 */
	@Override
	public void environmentChanged() {
		this.applyNewSettings();
	}
}