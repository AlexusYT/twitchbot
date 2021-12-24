package ru.alexus.twitchbot.eventsub;

import org.json.JSONObject;

import java.util.HashMap;

public class EventSubInfo {
	private final String id;
	private String status;
	private final String type;
	private final String version;
	private final HashMap<String, String> condition = new HashMap<>();
	private final String callback;
	private String secret;

	public EventSubInfo(String id, String type, String version, String callback, String secret) {
		this.id = id;
		this.type = type;
		this.version = version;
		this.callback = callback;
		this.secret = secret;
	}

	public EventSubInfo(JSONObject root) {

		id = root.getString("id");
		status = root.getString("status");
		version = root.getString("version");
		type = root.getString("type");
		for (var entry : root.getJSONObject("condition").toMap().entrySet()) {
			condition.put(entry.getKey(), (String) entry.getValue());
		}
		callback = root.getJSONObject("transport").getString("callback");
	}

	public EventSubInfo(JSONObject root, String secret) {
		this(root);
		this.secret = secret;
	}

	public String getVersion() {
		return version;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public HashMap<String, String> getCondition() {
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
