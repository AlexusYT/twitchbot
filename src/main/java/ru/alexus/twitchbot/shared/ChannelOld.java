package ru.alexus.twitchbot.shared;

import org.json.JSONObject;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.TwitchEventSubAPI;
import ru.alexus.twitchbot.eventsub.events.RedemptionAdd;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ru.alexus.twitchbot.Utils.sendPost;

public class ChannelOld {
	private int channelId;

	public HashMap<String, EventSubInfo> subscriptions = new HashMap<>();

	public String channelName;

	public void subscribeEvent(String type, Map<String, String> conditions) {
		try {
			Globals.log.info("Subscribing to event "+type+" for "+channelName);
			subscriptions.put(type, TwitchEventSubAPI.subscribeToEvent(type, "1", channelName+"/callback", conditions));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void subscriptionRevoked(EventSubInfo subInfo){
		Globals.log.info("Subscription "+subInfo.getType()+" revoked from "+channelName+". Reason: "+subInfo.getStatus());
		if(subInfo.getStatus().equals("authorization_revoked")){
			try{
				subscriptions.put(subInfo.getType(), TwitchEventSubAPI.resubscribeToEvent(subInfo));
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public void subscriptionNotification(EventSubInfo subInfo, JSONObject event) {
		Globals.log.info("Subscription "+subInfo.getType()+" notified for "+channelName);
		Globals.log.info("Event object: "+event);

		//Twitch.sendMsg(subInfo.getType(), this);
	}
	public void onRewardRedemption(EventSubInfo subInfo, RedemptionAdd event){
		/*UserOld user = this.getUserById(event.getUserId());
		Reward reward = event.getReward();
		Globals.log.info("User "+user.getDisplayName()+" redeemed reward "+reward+" in "+channelName);
		if(reward.getId().equals("044a7ea1-8e62-476d-b58a-30b215a778cd")){//buy coins
			//this.addCoins(user, 200);
			//Twitch.sendMsg(user.getDisplayName()+" купил 200 коинов за "+Utils.pluralizeMessagePoints(reward.getCost())+" канала", this);
		}else if(reward.getId().equals("188bcad5-82a8-4d19-a184-0c45bd5b0014")){//vip

		}*/

	}

	public static void initRoomState(){

		String secret = "qwer";

		EventSubInfo eventSubInfo = new EventSubInfo("", "channel.ban", "1", "https://alexus-twitchbot.herokuapp.com/alexus_xx/callback", secret);
		eventSubInfo.setStatus("webhook_callback_verification");

		String body = "{\"subscription\":{\"id\":\"1aec1360-741a-4abe-bad7-d59e14a7ee27\",\"status\":\"webhook_callback_verification_pending\",\"type\":\"channel.ban\",\"version\":\"1\",\"condition\":{\"broadcaster_user_id\":\"134945794\"},\"transport\":{\"method\":\"webhook\",\"callback\":\"https://alexus-twitchbot.herokuapp.com/alexus_xx/callback\"},\"created_at\":\"2021-12-15T13:02:27.704816431Z\",\"cost\":0},\"challenge\":\"cVLoQh4IRXLKBopxhiHYXn7mohbv1ZiTW_LBx8HaUY0\"}";

		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-Id", Globals.twitchClientId);
		String messageTimestamp = "2021-12-15T13:02:27.710416989Z";
		headers.put("Twitch-eventsub-message-timestamp", messageTimestamp);
		headers.put("Twitch-eventsub-message-type", "webhook_callback_verification");
		headers.put("Twitch-eventsub-subscription-type", "channel.ban");
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer "+Globals.appAccessToken.getToken());

		String messageId = "8f7cfd33-2109-4aea-97c3-633b432cec";
		headers.put("Twitch-eventsub-message-signature", "sha256="+ Utils.hmacSha256(messageId+messageTimestamp+body, secret));
		headers.put("Twitch-eventsub-message-id", messageId);
		try {
			sendPost("http://localhost/alexus_xx/callback", headers, body);
			//System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}


			/*subscribeEvent("channel.ban", Map.of("broadcaster_user_id", String.valueOf(channelId)));
			subscribeEvent("channel.unban", Map.of("broadcaster_user_id", String.valueOf(channelId)));
			subscribeEvent("channel.channel_points_custom_reward_redemption.add", Map.of("broadcaster_user_id", String.valueOf(channelId)));*/


	}


}
