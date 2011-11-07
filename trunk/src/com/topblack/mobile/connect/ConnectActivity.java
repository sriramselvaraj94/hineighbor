package com.topblack.mobile.connect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ConnectActivity extends Activity {

	private final static String LOG_TAG = SettingsActivity.class
			.getSimpleName();

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

		this.initMockData();

		if (!checkWifiStatus()) {
			// this.showToast("No wifi network!");
		}
	}

	private void refreshServices() {
		new Thread(new Runnable() {
			public void run() {
				Log.i(LOG_TAG, "refresh services...");
				
				testUpdateService();
				
				JmDNS registry = null;
				try {
					registry = JmDNS.create();
				
					
					List<String> enabledServices = LocalSettings
							.getEnabledServices(ConnectActivity.this);
					for (String serviceName : enabledServices) {
						String serviceType = LocalSettings
								.getServiceTypeByTitle(serviceName);
						Log.i(LOG_TAG, "Register service..." + serviceType);
						
						String text = "Test service";
						Map<String, byte[]> properties = new HashMap<String, byte[]>();
						properties.put("srvname", text.getBytes());
						ServiceInfo service = ServiceInfo.create(serviceType,
								"apache-someuniqueid", 80, 0, 0, true, properties);
						
						registry.registerService(service);
						Log.i(LOG_TAG, "List service..." + service.getType());
						ServiceInfo[] services = registry.list(service.getType());
						Log.i(LOG_TAG, services.length + " services ("
								+ serviceType + ") is found.");
					}
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

	private void initMockData() {
		serviceInfoList.add(new ServiceInfoViewModel("cn-pdc-d-leonq",
				"192.168.0.1:8080"));
		this.notifyServiceListChanged();
	}

	private boolean checkWifiStatus() {
		try {
			ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); // Network
			State wifiState = conMan.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).getState();
			return wifiState == State.CONNECTED;
		} catch (Exception ex) {
			Log.e(this.getClass().getName(), "Check Wifi Status Failed!", ex);
			this.showToast(ex.toString());
			return false;
		}

	}

	private void notifyServiceListChanged() {
		ListView servicesListView = (ListView) this
				.findViewById(R.id.servicesListView);
		((SimpleAdapter) servicesListView.getAdapter()).notifyDataSetChanged();
	}

	private void testUpdateService() {
		String text = "Test hypothetical web server";
		Map<String, byte[]> properties = new HashMap<String, byte[]>();
		properties.put("srvname", text.getBytes());
		ServiceInfo service = ServiceInfo.create("_html._tcp.local.",
				"apache-someuniqueid", 80, 0, 0, true, properties);
		JmDNS registry = null;
		Exception occuredEx = null;
		int serviceCount = 0;
		try {
			registry = JmDNS.create();
			registry.registerService(service);

			ServiceInfo[] services = registry.list(service.getType());

			serviceCount = services.length;
			Log.d(LOG_TAG, serviceCount + " services found");
		} catch (IOException ex) {
			this.showToast(ex.toString());
			occuredEx = ex;
		} finally {
			if (registry != null) {
				try {
					registry.close();
				} catch (IOException ex) {
					this.showToast(ex.toString());
					occuredEx = ex;
				}
			}
		}
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private void showToast(int id) {
		ListView servicesListView = (ListView) this
				.findViewById(R.id.servicesListView);

		Toast.makeText(this, servicesListView.getItemAtPosition(id).toString(),
				Toast.LENGTH_SHORT).show();
	}
}