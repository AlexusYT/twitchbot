package ru.alexus.twitchbot.eventsub;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EventSubInfo {
	private final String id;
	private final String status;
	private final String type;
	private final HashMap<String, Object> condition;
	private final String callback;
	private String secret;


	public EventSubInfo(JSONObject root){

		id = root.getString("id");
		status = root.getString("status");
		type = root.getString("type");
		condition = (HashMap<String, Object>) root.getJSONObject("condition").toMap();
		callback = root.getJSONObject("transport").getString("callback");
	}

	public EventSubInfo(JSONObject root, String secret){
		this(root);
		this.secret = secret;
	}

	public String getSecret() {
		return secret;
	}

	public String getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}

	public HashMap<String, Object> getCondition() {
		return condition;
	}

	public String getCallback() {
		return callback;
	}

	@Override
	public String toString() {
		return "EventSubInfo{" + "id='" + id + '\'' +
				", status='" + status + '\'' +
				", type='" + type + '\'' +
				'}';
	}
}
