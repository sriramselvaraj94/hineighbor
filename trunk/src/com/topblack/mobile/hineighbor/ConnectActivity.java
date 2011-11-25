package com.topblack.mobile.hineighbor;

import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceInfo;

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
import android.widget.Toast;

/**
 * The main activity, which list the available services in the local network.
 * 
 * @author 10115154
 */
public class ConnectActivity extends Activity implements INotificationListener{

	private final static String LOG_TAG = ConnectActivity.class.getSimpleName();

	// ===================View Models===================
	// The view model of the service info list
	private List<ServiceInfoViewModel> serviceInfoList = new ArrayList<ServiceInfoViewModel>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.initViewModels();
		this.initEventHandlers();

		HiNeighborService.startService(this);

		Intent intent = new Intent();
		intent.setClass(this, HiNeighborService.class);
		this.startService(intent);
		this.bindService(intent, this.serviceConnection,
				Context.BIND_AUTO_CREATE);
		
	}

	@Override
	public void onDestroy() {
		hiNeighborService.unregisterListener(this);
		this.unbindService(this.serviceConnection);
		super.onDestroy();
	}
	
	private void refreshServices() {
		final List<ServiceInfo> availableServices = this.hiNeighborService.getAvailableServices();
		
		new Thread(new Runnable() {
			public void run() {
				Log.i(LOG_TAG, "refresh services...");
				try {
					ConnectActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							serviceInfoList.clear();
							for (ServiceInfo service : availableServices) {
								serviceInfoList.add(new ServiceInfoViewModel(
										service));
							}
							notifyServiceListChanged();
						}
					});
				} catch (Exception ex) {
					ex.printStackTrace();
					Log.e(LOG_TAG, ex.toString());
				}
			}
		}).start();
	}

	private void onRefreshButtonClicked(View source) {
		this.showToast("Clicked " + ((Button) source).getText());
		this.refreshServices();
	}

	private void onSettingsButtonClicked(View source) {
		Intent intent = new Intent();
		intent.setClass(this, SettingsActivity.class);
		this.startActivityForResult(intent, RESULT_OK);
	}

	private void onServiceListItemSelected(AdapterView<?> arg0, View arg1,
			int arg2, long arg3) {
		ServiceInfoViewModel selectedService = serviceInfoList.get(arg2);
		Log.i(LOG_TAG, "Selected service at " + selectedService.getServiceIp());
		Intent intent = new Intent();
		intent.putExtra("ServiceName", selectedService.getServiceName());
		intent.putExtra("ServiceAddress", selectedService.getServiceIp());
		intent.setClass(this, ChatActivity.class);
		this.startActivityForResult(intent, RESULT_OK);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (resultCode) {
		case RESULT_OK:
			break;
		default:
			break;
		}
	}

	private void initViewModels() {
		ListView servicesListView = (ListView) this
				.findViewById(R.id.servicesListView);
		ListAdapter servicesListAdapter = new SimpleAdapter(this,
				serviceInfoList, android.R.layout.simple_list_item_2,
				new String[] { "serviceName", "serviceDesc" }, new int[] {
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
		ListView servicesListView = (ListView) this
				.findViewById(R.id.servicesListView);
		((SimpleAdapter) servicesListView.getAdapter()).notifyDataSetChanged();
	}


	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private IHiNeighborService hiNeighborService = null;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			hiNeighborService = (IHiNeighborService) service;
			hiNeighborService.registerListener(ConnectActivity.this);
			Log.v(LOG_TAG, "on service connected.");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			hiNeighborService = null;
		}
	};

	/* (non-Javadoc)
	 * @see com.topblack.mobile.hineighbor.INotificationListener#notificationReceived(java.lang.String)
	 */
	@Override
	public void notificationReceived(String notificationId) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "Received notification " + notificationId);
	}
}