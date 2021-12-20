package ru.alexus.twitchbot.web;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.springframework.lang.NonNull;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.bot.TwitchBot;
import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.TwitchEventSubAPI;
import ru.alexus.twitchbot.shared.ChannelOld;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.Database;
import ru.alexus.twitchbot.twitch.Twitch;

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Web {
	private HttpServer server;
	private final LinkedHashMap<String, BotChannel> channels = new LinkedHashMap<>();
	private final int port;
	private final Twitch twitch;
	private final Database botDatabase;

	public Web(int port, Twitch twitch, Database botDatabase){
		this.port = port;
		this.twitch = twitch;
		this.botDatabase = botDatabase;
	}

	public void start() throws IOException {
		new Thread(() -> {
			Globals.log.info("Getting an app access token");

//https://id.twitch.tv/oauth2/authorize?response_type=code&client_id=cxxcdpgmikulrcqf6wb899qxgfgrkw&redirect_uri=http://localhost&scope=viewing_activity_read%20channel:read:subscriptions%20channel:moderate%20channel:manage:redemptions&state=c3ab8aa609ea11e793ae92361f002671'
			while (Globals.appAccessToken==null){
				try {
					Globals.appAccessToken = TwitchEventSubAPI.getAppAccessToken("viewing_activity_read", "channel:read:subscriptions", "channel:moderate", "channel:manage:redemptions");

					System.out.println(Globals.appAccessToken);
				}catch (Exception e) {
					Globals.log.error("Failed to get app access token", e);
					try {
						TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException ignored) {}
				}
			}
			Globals.log.info("App access token received");
		}).start();
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();

		try {
			ResultSet set = botDatabase.executeSelect("channels");
			while (set.next()){
				BotChannel channel = new BotChannel(set, twitch);
				if(!channel.isActivated()) continue;
				channels.put(channel.getName(), channel);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Globals.log.info("Waiting an app access token");
		while (Globals.appAccessToken==null){
			try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (InterruptedException ignored) {}
		}
	}

	public void unsubscribeAllEvents(){
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
		}catch (Exception e){
			Globals.log.error("Failed to unsubscribe events", e);
		}
	}

	public void subscribeChannelsEvents(){
		for(BotChannel channel : channels.values()){
			ChannelCallback callback =  new ChannelCallback(channel);
			server.createContext("/"+channel.getName()+"/callback", callback);
			for(Map.Entry<String, EventSubInfo> event : channel.getEvents().entrySet()){
				HashMap<String, String> conditions = new HashMap<>();
				switch (event.getKey()) {
					case "stream.offline", "stream.online", "channel.channel_points_custom_reward_redemption.add" ->
							conditions.put("broadcaster_user_id", String.valueOf(channel.getTwitchID()));
				}
				try {
					event.setValue(TwitchEventSubAPI.subscribeToEvent(event.getKey(), "1", channel.getName()+"/callback", conditions));
					Globals.log.info("Event subscribed: " + event.getValue());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void startWeb(){

		boolean tryingStart = true;
		int tries = 0;

		while (tryingStart){
			try {
				Globals.log.info("Starting web-server");
				server = HttpServer.create(new InetSocketAddress(port), 0);
				server.createContext("/", new MyHandler());
				server.setExecutor(null); // creates a default executor
				server.start();
				tryingStart = false;
				Globals.log.info("Successfully started web-server");

				new Thread(()->{
					while (true){
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

			}catch (Exception e){
				Globals.log.error("Failed to start web-server. Retrying", e);
			}
			try {
				Thread.sleep((long) Math.pow(2, tries)*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tries++;
		}
	}

	private static void clearCallbacks(){

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

			}catch (Exception e){

			}
			//String response = "This is the response "+code;
			t.sendResponseHeaders(200, 0/*response.length()*/);
			OutputStream os = t.getResponseBody();
			//os.write(response.getBytes());
			os.close();
		}
	}
}
