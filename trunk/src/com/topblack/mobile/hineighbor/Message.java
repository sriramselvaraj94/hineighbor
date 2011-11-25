/**
 * File:    Message.java
 * Author : 10115154
 * Created: Nov 23, 2011
 * Copyright 2011, Eastman Kodak Company
 */
package com.topblack.mobile.hineighbor;

import java.util.UUID;

/**
 * @author 10115154
 * 
 */
public class Message {
	private String messageId = null;

	private long createdTime = 0;

	private Neighbor targetId = null;

	private Neighbor sourceId = null;

	private String messageContent = null;

	public Message(String subType, String targetId, String messageContent) {
		this.sourceId = new Neighbor(LocalEnvironment.LocalIdentity);
		this.targetId = new Neighbor(targetId);
		this.messageContent = messageContent;
		this.createdTime = System.currentTimeMillis();
		this.messageId = subType + ":" + UUID.randomUUID().toString();
	}

	public Message(String messageId, long createdTime, String targetId,
			String sourceId, String messageContent) {
		super();
		this.messageId = messageId;
		this.createdTime = createdTime;
		this.targetId = new Neighbor(targetId);
		this.sourceId = new Neighbor(sourceId);
		this.messageContent = messageContent;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(messageId);
		sb.append("#");
		sb.append(createdTime);
		sb.append("#");
		sb.append(targetId);
		sb.append("#");
		sb.append(sourceId);
		sb.append("|");
		sb.append(messageContent);

		return sb.toString();
	}

	public byte[] toBytes() {
		try {
			String stringContent = this.toString();
			return stringContent.getBytes("UTF-8");
		} catch (Exception ex) {
			return new byte[0];
		}
	}

	public static Message fromBytes(byte[] content) {
		try {
			String stringContent = new String(content, "UTF-8");

			int separateIndex = stringContent.indexOf("|");
			String messageContent = stringContent.substring(separateIndex + 1);
			String messageHeader = stringContent.substring(0, separateIndex);
			String[] headerSections = messageHeader.split("#");
			String messageId = headerSections[0];
			long createdTime = Long.parseLong(headerSections[1]);
			String targetId = headerSections[2];
			String sourceId = headerSections[3];

			return new Message(messageId, createdTime, targetId, sourceId,
					messageContent);
		} catch (Exception ex) {
			return null;
		}
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public Neighbor getTargetId() {
		return targetId;
	}

	public void setTargetId(Neighbor targetId) {
		this.targetId = targetId;
	}

	public Neighbor getSourceId() {
		return sourceId;
	}

	public void setSourceId(Neighbor sourceId) {
		this.sourceId = sourceId;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	
	
}
