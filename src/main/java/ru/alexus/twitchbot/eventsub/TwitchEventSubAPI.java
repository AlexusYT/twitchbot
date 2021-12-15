package ru.alexus.twitchbot.eventsub;

import org.json.JSONObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;

import java.io.IOException;
import java.util.Arrays;
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
			delim="%20";
		}

		JSONObject root = new JSONObject(Utils.sendPost("https://id.twitch.tv/oauth2/token", null, "client_id="+ Globals.twitchClientId +
				"&client_secret="+Globals.twitchSecret +
				"&grant_type=client_credentials" +
				"&scope="+scopesString));
		return new AppAccessToken(root);
	}

	public static EventSubInfo subscribeToEvent(String type, String version, String callback, Map<String, String> condition) throws IOException {
		String secret = Utils.generateSecret();
		JSONObject body = new JSONObject();
		body.put("type", type);
		body.put("version", version);
		body.put("condition", new JSONObject(condition));
		JSONObject transport = new JSONObject();
		transport.put("method", "webhook");
		transport.put("callback", "https://alexus-twitchbot.herokuapp.com/"+callback);
		transport.put("secret", secret);
		body.put("transport", transport);
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-Id", Globals.twitchClientId);;
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer "+Globals.appAccessToken.getToken());
		String respStr = Utils.sendPost("https://api.twitch.tv/helix/eventsub/subscriptions", headers, body.toString());
		System.out.println(respStr);
		JSONObject response = new JSONObject(respStr);
		if(response.has("error")){
			throw new RuntimeException(response.getString("error")+": "+response.getString("message"));
		}
		return new EventSubInfo(response.getJSONArray("data").getJSONObject(0), secret);
	}

	public static LinkedList<EventSubInfo> getSubscribedEvent(String filterByType, String page) throws IOException {
		String body = "";
		if(filterByType!=null){
			body="type="+filterByType;
		}
		if(page!=null){
			body="after="+page;
		}
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-Id", Globals.twitchClientId);;
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer "+Globals.appAccessToken.getToken());
		String respStr = Utils.sendGet("https://api.twitch.tv/helix/eventsub/subscriptions", headers, body);

		JSONObject response = new JSONObject(respStr);
		if(response.has("pagination"))
			Globals.log.info("Subs pagination "+response.get("pagination"));
		LinkedList<EventSubInfo> events = new LinkedList<>();
		for (Object data : response.getJSONArray("data")){
			EventSubInfo event = new EventSubInfo((JSONObject) data);
			events.add(event);
		}
		return events;
	}

	public static int deleteSubscribedEvent(EventSubInfo event) throws IOException {
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-Id", Globals.twitchClientId);;
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer "+Globals.appAccessToken.getToken());

		return Utils.sendDelete("https://api.twitch.tv/helix/eventsub/subscriptions", headers, "id="+event.getId());
	}
}
