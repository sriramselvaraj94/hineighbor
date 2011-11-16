/**
 * File:    ChatActivity.java
 * Author : 10115154
 * Created: Nov 1, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import com.topblack.mobile.hineighbor.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * @author 10115154
 * 
 */
public class ChatActivity extends Activity {
	private final static String LOG_TAG = SettingsActivity.class
			.getSimpleName();

	private String activityTargetServiceName = null;
	private String activityTargetServiceAddress = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		activityTargetServiceName = getIntent().getExtras().getString(
				"ServiceName");
		activityTargetServiceAddress = getIntent().getExtras().getString(
				"ServiceAddress");
		Log.v(LOG_TAG, "Target Service:" + activityTargetServiceName + "@"
				+ activityTargetServiceAddress);

		((TextView) this.findViewById(R.id.ChatTitle))
				.setText(activityTargetServiceName + "@"
						+ activityTargetServiceAddress);
	}
}
