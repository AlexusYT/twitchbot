package ru.alexus.twitchbot.eventsub;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.eventsub.events.RewardRedemption;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TwitchEventSubAPI {

	@NonNull
	public static AppAccessToken getAppAccessToken(String... scopes) throws IOException {
		StringBuilder scopesString = new StringBuilder();
		String delim = "";
		for (String scope : scopes) {
			scopesString.append(delim).append(scope);
			delim = "%20";
		}
		HashMap<String, String> body = new HashMap<>();
		body.put("client_id", Globals.twitchClientId);
		body.put("client_secret", Globals.twitchSecret);
		body.put("grant_type", "client_credentials");
		body.put("scope", scopesString.toString());

		JSONObject root = new JSONObject(Utils.sendPost("https://id.twitch.tv/oauth2/token", null, body));
		return new AppAccessToken(root);
	}

	/*

			//POST https://id.twitch.tv/oauth2/token
			//    ?client_id=<your client ID>
			//    &client_secret=<your client secret>
			//    &code=<authorization code received above>
			//    &grant_type=authorization_code
			//    &redirect_uri=<your registered redirect URI>
	 */
	public static UserAccessToken getUserAccessToken(String code) throws IOException {
		JSONObject root = new JSONObject(Utils.sendPost("https://id.twitch.tv/oauth2/token", null, "client_id=" + Globals.twitchClientId +
				"&client_secret=" + Globals.twitchSecret +
				"&code=" + code +
				"&grant_type=authorization_code" +
				"&redirect_uri=http://localhost"));
		System.out.println(root);
		return new UserAccessToken(root);
	}

	public static EventSubInfo resubscribeToEvent(EventSubInfo oldEvent) throws IOException {
		return TwitchEventSubAPI.subscribeToEvent(oldEvent.getType(), oldEvent.getVersion(), oldEvent.getCallback(), oldEvent.getCondition());
	}


	@Contract("_, _, _, _ -> new")
	public static @NotNull EventSubInfo subscribeToEvent(String type, String version, String callback, Map<String, String> condition) throws IOException {
		String secret = Utils.generateSecret();
		System.out.println(secret);
		JSONObject body = new JSONObject();
		body.put("type", type);
		body.put("version", version);
		body.put("condition", new JSONObject(condition));
		JSONObject transport = new JSONObject();
		transport.put("method", "webhook");
		transport.put("callback", Globals.serverAddress + callback);
		transport.put("secret", secret);
		body.put("transport", transport);
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-Id", Globals.twitchClientId);
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer " + Globals.appAccessToken.getToken());
		String respStr = Utils.sendPost("https://api.twitch.tv/helix/eventsub/subscriptions", headers, body.toString());
		JSONObject response = new JSONObject(respStr);
		if (response.has("error")) {
			throw new RuntimeException(type + ": " + response.getString("error") + ": " + response.getString("message"));
		}
		return new EventSubInfo(response.getJSONArray("data").getJSONObject(0), secret);
	}

	public static LinkedList<EventSubInfo> getSubscribedEvent(String filterByType, String page) throws IOException {

		HashMap<String, String> query = new HashMap<>();
		if (filterByType != null) {
			query.put("type", filterByType);
		}
		if (page != null) {
			query.put("after", page);
		}
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-Id", Globals.twitchClientId);
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer " + Globals.appAccessToken.getToken());
		String respStr = Utils.sendGet("https://api.twitch.tv/helix/eventsub/subscriptions", headers, query);
		System.out.println(respStr);
		JSONObject response = new JSONObject(respStr);
		if (response.has("pagination"))
			Globals.log.info("Subs pagination " + response.get("pagination"));
		LinkedList<EventSubInfo> events = new LinkedList<>();
		for (Object data : response.getJSONArray("data")) {
			EventSubInfo event = new EventSubInfo((JSONObject) data);
			events.add(event);
		}
		return events;
	}

	public static int deleteSubscribedEvent(EventSubInfo event) throws IOException {
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-Id", Globals.twitchClientId);
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer " + Globals.appAccessToken.getToken());

		return Utils.sendDelete("https://api.twitch.tv/helix/eventsub/subscriptions", headers, Map.of("id", event.getId()));
	}


	public static RewardRedemption updateRedemption(RewardRedemption redemption, RewardRedemption.EnumRedemptionStatus status) throws IOException {
		if (redemption.getStatus() == status) return redemption;
		JSONObject body = new JSONObject();
		body.put("status", status);
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-Id", Globals.twitchClientId);
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer " + Globals.appAccessToken.getToken());
		HashMap<String, String> query = new HashMap<>();
		query.put("id", redemption.getId());
		query.put("broadcaster_id", String.valueOf(redemption.getBroadcasterId()));
		query.put("reward_id", redemption.getReward().getId());

		String respStr = Utils.sendPatch("https://api.twitch.tv/helix/channel_points/custom_rewards/redemptions", headers, query, body.toString());
		JSONObject response = new JSONObject(respStr);
		if (response.has("error")) {
			throw new RuntimeException(redemption + ": " + response.getString("error") + ": " + response.getString("message"));
		}
		return new RewardRedemption(response.getJSONArray("data").getJSONObject(0));
	}
}
