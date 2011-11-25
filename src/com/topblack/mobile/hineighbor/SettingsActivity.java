/**
 * File:    SettingsActivity.java
 * Author : 10115154
 * Created: Nov 1, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * @author 10115154
 * 
 */
public class SettingsActivity extends Activity {
	// ===================View Models===================

	// The view model of the service info list
	private final static String[] availableServices = LocalEnvironment
			.getSupportedServiceTitles();

	private final static String LOG_TAG = SettingsActivity.class
			.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appsettings);

		this.initViewModels();

		Log.i(LOG_TAG, "UI created!");
	}

	private void initViewModels() {
		ListView servicesListView = (ListView) this
				.findViewById(R.id.serviceControlListView);

		ListAdapter servicesListAdapter = new ArrayAdapter(this,
				android.R.layout.simple_list_item_multiple_choice,
				availableServices);

		servicesListView.setAdapter(servicesListAdapter);

		this.LoadSharedPreferences();
	}

	private void notifyServiceListChanged() {
		ListView servicesListView = (ListView) this
				.findViewById(R.id.serviceControlListView);
		((SimpleAdapter) servicesListView.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.StoreSharedPreferences();
			this.setResult(1);
			this.finish();
		}

		return true;
	}

	private void LoadSharedPreferences() {
		Log.i(LOG_TAG, "Load Shared Preferences");
		ListView servicesListView = (ListView) this
				.findViewById(R.id.serviceControlListView);

		for (int i = 0; i < servicesListView.getCount(); i++) {
			String checkedService = servicesListView.getItemAtPosition(i)
					.toString();
			boolean isSettingSelected = LocalEnvironment.isOptionEnabled(this,
					checkedService, false);
			servicesListView.setItemChecked(i, isSettingSelected);
		}
		
		EditText nickNameEdit = (EditText) this.findViewById(R.id.NickNameEdit);
		nickNameEdit.setText(LocalEnvironment.getLocalName(this));
	}

	private void StoreSharedPreferences() {
		Log.i(LOG_TAG, "Store Shared Preferences");
		ListView servicesListView = (ListView) this
				.findViewById(R.id.serviceControlListView);

		SparseBooleanArray boolArray = servicesListView
				.getCheckedItemPositions();
		for (int i = 0; i < boolArray.size(); i++) {
			LocalEnvironment.enableOption(this, availableServices[i],
					boolArray.get(i));
		}
		
		EditText nickNameEdit = (EditText) this.findViewById(R.id.NickNameEdit);
		String nickName = nickNameEdit.getText().toString();
		LocalEnvironment.setLocalName(this, nickName);
	}
}
