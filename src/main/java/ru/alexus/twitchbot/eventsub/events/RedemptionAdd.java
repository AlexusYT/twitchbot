package ru.alexus.twitchbot.eventsub.events;

import org.json.JSONObject;
import ru.alexus.twitchbot.eventsub.objects.Reward;

public class RedemptionAdd extends Event{
	private final String id;
	private final int userId;
	private final String userLogin;
	private final String userName;
	private final String userInput;
	private final String status;
	private final Reward reward;
	private final String redeemedAt;

	public RedemptionAdd(JSONObject object) {
		super(object);
		this.id = object.getString("id");
		this.userId = Integer.parseInt(object.getString("user_id"));
		this.userLogin = object.getString("user_login");
		this.userName = object.getString("user_name");
		this.userInput = object.getString("user_input");
		this.status = object.getString("status");
		this.reward = new Reward(object.getJSONObject("reward"));
		this.redeemedAt = object.getString("redeemed_at");
	}

	public String getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserInput() {
		return userInput;
	}

	public String getStatus() {
		return status;
	}

	public Reward getReward() {
		return reward;
	}

	public String getRedeemedAt() {
		return redeemedAt;
	}
}
