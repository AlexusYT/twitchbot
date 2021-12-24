package ru.alexus.twitchbot.shared;

import org.json.JSONObject;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.TwitchEventSubAPI;
import ru.alexus.twitchbot.eventsub.events.RewardRedemption;

import java.util.HashMap;
import java.util.Map;

public class ChannelOld {
	private int channelId;

	public HashMap<String, EventSubInfo> subscriptions = new HashMap<>();

	public String channelName;

	public void subscribeEvent(String type, Map<String, String> conditions) {
		try {
			Globals.log.info("Subscribing to event " + type + " for " + channelName);
			subscriptions.put(type, TwitchEventSubAPI.subscribeToEvent(type, "1", channelName + "/callback", conditions));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void subscriptionRevoked(EventSubInfo subInfo) {
		Globals.log.info("Subscription " + subInfo.getType() + " revoked from " + channelName + ". Reason: " + subInfo.getStatus());
		if (subInfo.getStatus().equals("authorization_revoked")) {
			try {
				subscriptions.put(subInfo.getType(), TwitchEventSubAPI.resubscribeToEvent(subInfo));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void subscriptionNotification(EventSubInfo subInfo, JSONObject event) {
		Globals.log.info("Subscription " + subInfo.getType() + " notified for " + channelName);
		Globals.log.info("Event object: " + event);

		//Twitch.sendMsg(subInfo.getType(), this);
	}

	public void onRewardRedemption(EventSubInfo subInfo, RewardRedemption event) {
		/*UserOld user = this.getUserById(event.getUserId());
		Reward reward = event.getReward();
		Globals.log.info("User "+user.getDisplayName()+" redeemed reward "+reward+" in "+channelName);
		if(reward.getId().equals("044a7ea1-8e62-476d-b58a-30b215a778cd")){//buy coins
			//this.addCoins(user, 200);
			//Twitch.sendMsg(user.getDisplayName()+" купил 200 коинов за "+Utils.pluralizeMessagePoints(reward.getCost())+" канала", this);
		}else if(reward.getId().equals("188bcad5-82a8-4d19-a184-0c45bd5b0014")){//vip

		}*/

	}


}
