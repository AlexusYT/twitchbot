package ru.alexus.twitchbot.eventsub.objects;

import org.json.JSONObject;

public class Reward {
	String id;
	String title;
	int cost;
	String prompt;

	public Reward(JSONObject object) {
		this.id = object.getString("id");
		this.title = object.getString("title");
		this.cost = object.getInt("cost");
		this.prompt = object.getString("prompt");
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public int getCost() {
		return cost;
	}

	public String getPrompt() {
		return prompt;
	}

	@Override
	public String toString() {
		return "Reward{" +
				"id='" + id + '\'' +
				", title='" + title + '\'' +
				", cost=" + cost +
				'}';
	}
}
