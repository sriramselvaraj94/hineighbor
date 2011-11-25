/**
 * File:    ChatActivity.java
 * Author : 10115154
 * Created: Nov 1, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author 10115154
 * 
 */
public class ChatActivity extends Activity implements INotificationListener {
	private final static String LOG_TAG = ChatActivity.class.getSimpleName();

	private String targetNeighborName = null;
	private String targetNeighborAddress = null;
	private String targetNeighborIdentity = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		targetNeighborName = getIntent().getExtras().getString("NeighborName");
		targetNeighborAddress = getIntent().getExtras().getString(
				"NeighborAddress");
		targetNeighborIdentity = getIntent().getExtras().getString(
				"NeighborIdentity");
		Log.v(LOG_TAG, "Target Service:" + targetNeighborIdentity + "@"
				+ targetNeighborAddress);

		((TextView) this.findViewById(R.id.ChatTitle))
				.setText(targetNeighborName + "@" + targetNeighborAddress);

		((Button) this.findViewById(R.id.sendButton))
				.setOnClickListener(new Button.OnClickListener() {
					public void onClick(View source) {
						sendEnteredMessage();
					}
				});

		Intent intent = new Intent();
		intent.setClass(this, HiNeighborService.class);
		this.bindService(intent, this.serviceConnection,
				Context.BIND_AUTO_CREATE);
	}

	private void sendEnteredMessage() {
		EditText textView = ((EditText) this
				.findViewById(R.id.chatInputTextEdit));
		textView.setTextColor(Color.BLACK);
		String enteredMessage = textView.getText().toString();
		Message sentMessage = this.hiNeighborService.postMessage(
				targetNeighborIdentity, enteredMessage);
		this.showMessage(sentMessage, true);
		textView.setText("");
	}

	private void showMessage(final Message message, final boolean outgoing) {

		this.runOnUiThread(new Runnable() {
			public void run() {
				LinearLayout chatHistoryView = ((LinearLayout) ChatActivity.this
						.findViewById(R.id.ChatHistoryList));
				TextView messageTitleView = new TextView(ChatActivity.this);
				String timestamp = DataConvertUtility.timeFormat
						.format(new Date(message.getCreatedTime()));
				messageTitleView.setText(message.getSourceId().getNickName()
						+ " - " + timestamp);
				messageTitleView.setPadding(5, 4, 5, 2);
				messageTitleView.setTextSize(14);

				messageTitleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

				TextView messageContentView = new TextView(ChatActivity.this);
				messageContentView.setText(message.getMessageContent());

				messageContentView.setPadding(5, 2, 5, 4);
				messageContentView.setTextSize(14);

				messageTitleView.setTextColor(outgoing ? Color.GRAY
						: Color.BLACK);
				messageContentView.setTextColor(Color.BLACK);
				chatHistoryView.addView(messageTitleView);
				chatHistoryView.addView(messageContentView);
			}
		});
	}

	@Override
	public void onDestroy() {
		try {
			if (this.hiNeighborService != null) {
				hiNeighborService.unregisterListener(this);
				this.unbindService(this.serviceConnection);
			}
		} catch (Exception ex) {
			Log.w(LOG_TAG,
					"Exception occur during destroying the chat activity." + ex);
		}
		super.onDestroy();
	}

	private IHiNeighborService hiNeighborService = null;
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			hiNeighborService = (IHiNeighborService) service;
			hiNeighborService.registerListener(ChatActivity.this);
			Log.v(LOG_TAG, "on service connected.");

			List<Message> unreadMessages = hiNeighborService.getUnreadMessages(
					ChatActivity.this.targetNeighborIdentity, true);
			for (Message message : unreadMessages) {
				ChatActivity.this.showMessage(message, false);
			}

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
		Message message = hiNeighborService.getMessage(notificationId, false);
		if (null == message) {
			Log.w(LOG_TAG, "Got null message!");
			return;
		}
		if (message.getSourceId().getIdentity()
				.equals(this.targetNeighborIdentity)) {
			Log.i(LOG_TAG, message.toString());
			// TODO, if the message is associated with this chat activity, mark
			// the message as read.
			Message messageToShow = hiNeighborService.getMessage(
					notificationId, true);
			this.showMessage(messageToShow, false);
		} else {
			Log.d(LOG_TAG,
					"Received message, not an active chat."
							+ message.toString());
		}
	}
}
