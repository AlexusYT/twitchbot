package ru.alexus.twitchbot.eventsub.events;

import org.json.JSONObject;
import ru.alexus.twitchbot.eventsub.TwitchEventSubAPI;
import ru.alexus.twitchbot.eventsub.objects.Reward;

import java.util.Locale;

public class RewardRedemption extends Event {

	public enum EnumRedemptionStatus {
		UNFULFILLED,
		FULFILLED,
		CANCELED
	}

	private String id;
	private int userId;
	private String userLogin;
	private String userName;
	private String userInput;
	private EnumRedemptionStatus status;
	private Reward reward;
	private String redeemedAt;

	public RewardRedemption(JSONObject object) {
		super(object);
		this.id = object.getString("id");
		this.userId = Integer.parseInt(object.getString("user_id"));
		this.userLogin = object.getString("user_login");
		this.userName = object.getString("user_name");
		this.userInput = object.getString("user_input");
		this.status = EnumRedemptionStatus.valueOf(object.getString("status").toUpperCase(Locale.ROOT));
		this.reward = new Reward(object.getJSONObject("reward"));
		this.redeemedAt = object.getString("redeemed_at");
	}

	private void copy(RewardRedemption redemption) {
		this.id = redemption.id;
		this.userId = redemption.userId;
		this.userLogin = redemption.userLogin;
		this.userName = redemption.userName;
		this.userInput = redemption.userInput;
		this.status = redemption.status;
		this.reward = redemption.reward;
		this.redeemedAt = redemption.redeemedAt;
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

	public EnumRedemptionStatus getStatus() {
		return status;
	}

	public Reward getReward() {
		return reward;
	}

	public String getRedeemedAt() {
		return redeemedAt;
	}

	public boolean cancel() {
		if (status == EnumRedemptionStatus.CANCELED) return true;
		try {
			copy(TwitchEventSubAPI.updateRedemption(this, EnumRedemptionStatus.CANCELED));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean fulfill() {
		if (status == EnumRedemptionStatus.FULFILLED) return true;
		try {
			copy(TwitchEventSubAPI.updateRedemption(this, EnumRedemptionStatus.FULFILLED));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "RewardRedemption{" +
				"userName='" + userName + '\'' +
				", status=" + status +
				", reward=" + reward +
				'}';
	}
}
