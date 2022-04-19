package ru.alexus.twitchbot.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.TwitchEventSubAPI;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotConfig;
import ru.alexus.twitchbot.twitch.Twitch;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ru.alexus.twitchbot.Utils.sendPost;

public class Web {
	private HttpServer server;
	private final int port;
	private final Twitch twitch;

	public Web(int port, Twitch twitch) {
		this.port = port;
		this.twitch = twitch;
	}

	public void start() throws IOException {
		new Thread(() -> {
			Globals.log.info("Getting an app access token");

//https://id.twitch.tv/oauth2/authorize?response_type=code&client_id=cxxcdpgmikulrcqf6wb899qxgfgrkw&redirect_uri=http://localhost&scope=viewing_activity_read%20channel:read:subscriptions%20channel:moderate%20channel:manage:redemptions&state=c3ab8aa609ea11e793ae92361f002671'
			while (Globals.appAccessToken == null) {
				try {
					Globals.appAccessToken = TwitchEventSubAPI.getAppAccessToken("viewing_activity_read", "channel:read:subscriptions", "channel:moderate", "channel:manage:redemptions");

					System.out.println(Globals.appAccessToken);
				} catch (Exception e) {
					Globals.log.error("Failed to get app access token", e);
					try {
						TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException ignored) {
					}
				}
			}
			Globals.log.info("App access token received");
		}).start();
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();

		Globals.log.info("Waiting an app access token");
		while (Globals.appAccessToken == null) {
			try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (InterruptedException ignored) {
			}
		}
	}

	public void unsubscribeAllEvents() {
		if (!Utils.isWebHost()) return;
		Globals.log.info("Unsubscribing all subscriptions");
		try {
			LinkedList<EventSubInfo> events = TwitchEventSubAPI.getSubscribedEvent(null, null);
			for (EventSubInfo event : events) {
				try {
					int resultCode = TwitchEventSubAPI.deleteSubscribedEvent(event);
					if (resultCode == 204) Globals.log.info("Event unsubscribed: " + event);
					else Globals.log.error("Failed to unsubscribe event: " + event + ". Response code: " + resultCode);
				} catch (Exception e) {
					Globals.log.error("Failed to unsubscribe event: " + event, e);
				}
			}
			Globals.log.info("Unsubscribing finished");
		} catch (Exception e) {
			Globals.log.error("Failed to unsubscribe events", e);
		}
	}

	public void subscribeChannelsEvents() {
		for (BotChannel channel : BotConfig.botChannels.values()) {
			ChannelCallback callback = new ChannelCallback(channel);
			server.createContext("/" + channel.getName() + "/callback", callback);
			if (!Utils.isWebHost()) continue;
			for (Map.Entry<String, EventSubInfo> event : channel.getEvents().entrySet()) {
				HashMap<String, String> conditions = new HashMap<>();
				switch (event.getKey()) {
					case "stream.offline", "stream.online", "channel.channel_points_custom_reward_redemption.add" -> conditions.put("broadcaster_user_id", String.valueOf(channel.getTwitchID()));
				}
				try {
					event.setValue(TwitchEventSubAPI.subscribeToEvent(event.getKey(), "1", channel.getName() + "/callback", conditions));
					Globals.log.info("Event subscribed: " + event.getValue());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/*if (Utils.isWebHost()) return;
		new Thread(() -> {
			BotChannel channel = BotConfig.getChannel("daxtionoff");

			test(channel);
		}).start();*/
	}

	public static void test(BotChannel channel) {
		String type = "channel.channel_points_custom_reward_redemption.add";
		String secret = "ae7514603bed265d9de55bc5b092dd71dd6b1bdc42131bbd7be3d80d8390cc";

		EventSubInfo eventSubInfo = new EventSubInfo("", type, "1", "https://alexus-twitchbot.herokuapp.com/alexus_xx/callback", secret);
		eventSubInfo.setStatus("webhook_callback_verification");
		channel.getEvents().put(type, eventSubInfo);
		JSONObject object = new JSONObject();
		JSONObject subscription = new JSONObject();
		{
			subscription.put("id", "bb6f317e-7319-4e27-8114-91ebfecb9bb3");
			subscription.put("status", "enabled");
			subscription.put("type", type);
			subscription.put("version", "1");
			JSONObject condition = new JSONObject();
			{
				condition.put("broadcaster_user_id", "403234476");
			}
			subscription.put("condition", condition);
			JSONObject transport = new JSONObject();
			{
				transport.put("method", "webhook");
				transport.put("callback", "https://alexus-twitchbot.herokuapp.com/daxtionoff/callback");
			}
			subscription.put("transport", transport);
			subscription.put("created_at", "2021-12-20T19:47:53.497632801Z");
			subscription.put("cost", "0");
		}
		object.put("subscription", subscription);
		JSONObject event = new JSONObject();
		{
			event.put("broadcaster_user_id", "403234476");
			event.put("broadcaster_user_login", "daxtionoff");
			event.put("broadcaster_user_name", "daxtionoff");
			event.put("id", "0eea09f7-5b03-45c9-9fe1-033b61a0ab32");
			event.put("user_id", "134945794");//The stream type. Valid values are: live, playlist, watch_party, premiere, rerun.
			event.put("user_login", "alexus_xx");
			event.put("user_name", "Alexus_XX");
			event.put("user_input", "");
			event.put("status", "unfulfilled");
			event.put("redeemed_at", "2021-12-21T11:13:48.045286708Z");
			JSONObject reward = new JSONObject();
			{
				reward.put("id", "188bcad5-82a8-4d19-a184-0c45bd5b0014");
				reward.put("title", "Купить багикоины (В РАЗРАБОТКЕ)");
				reward.put("prompt", "НЕ ПОКУПАТЬ! НЕ РАБОТАЕТ. ВЫ ПРОСТО ПОТЕРЯЕТЕ БАЛЛЫ");
				reward.put("cost", "1");
			}
			event.put("reward", reward);
		}
		object.put("event", event);

		String body = object.toString();

		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-Id", Globals.twitchClientId);
		String messageTimestamp = "2021-12-20T19:51:30.844236831Z";
		headers.put("Twitch-eventsub-message-timestamp", messageTimestamp);
		headers.put("Twitch-eventsub-message-type", "notification");
		headers.put("Twitch-eventsub-subscription-type", type);
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer " + Globals.appAccessToken.getToken());

		String messageId = Utils.generateSecret();
		headers.put("Twitch-eventsub-message-signature", "sha256=" + Utils.hmacSha256(messageId + messageTimestamp + body, secret));
		headers.put("Twitch-eventsub-message-id", messageId);
		try {
			sendPost("http://localhost/daxtionoff/callback", headers, body);
			//System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void startWeb() {

		boolean tryingStart = true;
		int tries = 0;

		while (tryingStart) {
			try {
				Globals.log.info("Starting web-server");
				server = HttpServer.create(new InetSocketAddress(port), 0);
				server.createContext("/", new MyHandler());
				server.setExecutor(null); // creates a default executor
				server.start();
				tryingStart = false;
				Globals.log.info("Successfully started web-server");

				new Thread(() -> {
					while (true) {
						try {
							TimeUnit.MINUTES.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						clearCallbacks();
					}
				});
				Globals.log.info("Done");

				Globals.readyToBotStart = true;

			} catch (Exception e) {
				Globals.log.error("Failed to start web-server. Retrying", e);
			}
			try {
				Thread.sleep((long) Math.pow(2, tries) * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tries++;
		}
	}

	private static void clearCallbacks() {

		/*for (ChannelOld channel : Channels.getChannels().values()){
			HttpHandler handler = channel.httpContext.getHandler();
			if(handler instanceof ChannelCallback callback){
				callback.clearOldIds();
			}
		}*/
	}


	static class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			try {
				String twitchResponse = t.getRequestURI().toString();
				String code = twitchResponse.substring(twitchResponse.indexOf("code=") + 5, twitchResponse.indexOf("&"));
				try {
					Globals.userAccessToken = TwitchEventSubAPI.getUserAccessToken(code);
					System.out.println("User token: " + Globals.userAccessToken);

				} catch (Exception e) {
					Globals.log.error("Failed to get user token", e);
				}

			} catch (Exception ignored) {

			}
			String response = "This is the response ";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
