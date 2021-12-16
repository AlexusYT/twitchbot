package ru.alexus.twitchbot.eventsub.events;

import org.json.JSONObject;

public class Event {
	private final String broadcasterName;
	private final String broadcasterLogin;
	private final int broadcasterId;

	public Event(JSONObject object) {
		this.broadcasterName = object.getString("broadcaster_user_name");
		this.broadcasterLogin = object.getString("broadcaster_user_login");
		this.broadcasterId = Integer.parseInt(object.getString("broadcaster_user_id"));
	}

	public String getBroadcasterName() {
		return broadcasterName;
	}

	public String getBroadcasterLogin() {
		return broadcasterLogin;
	}

	public int getBroadcasterId() {
		return broadcasterId;
	}
}
