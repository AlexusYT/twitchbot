package ru.alexus.twitchbot.eventsub.events;

import org.json.JSONObject;
import ru.alexus.twitchbot.eventsub.objects.Reward;

public class StreamOnline extends Event{
	private final String id;
	private final String type;
	private final String startedAt;

	public StreamOnline(JSONObject object) {
		super(object);
		this.id = object.getString("id");
		this.type = object.getString("type");
		this.startedAt = object.getString("started_at");
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getStartedAt() {
		return startedAt;
	}
}
