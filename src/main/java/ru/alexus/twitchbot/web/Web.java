package ru.alexus.twitchbot.web;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.lang.NonNull;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.TwitchEventSubAPI;
import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.Channels;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Web {
	static HttpServer server;
	private static LinkedHashMap<String, HttpContext> channelContexts;

	public static void startWeb(){

		Globals.log.info("Getting app access token");

		while (Globals.appAccessToken==null){
			try {
				Globals.appAccessToken = TwitchEventSubAPI.getAppAccessToken("viewing_activity_read", "channel:read:subscriptions", "channel:moderate", "channel:manage:redemptions");
			}catch (Exception e) {
				Globals.log.error("Failed to get app access token", e);
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException ignored) {}
			}
		}
		Globals.log.info("App access token received");
		boolean tryingStart = true;
		int tries = 0;

		while (tryingStart){
			try {
				Globals.log.info("Starting web-server");
				String port = System.getenv("PORT");
				if(port==null) port = "80";
				server = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);
				server.createContext("/", new MyHandler());
				server.setExecutor(null); // creates a default executor
				server.start();
				tryingStart = false;
				Globals.log.info("Successfully started web-server");

				Globals.log.info("Unsubscribing all subscriptions");
				LinkedList<EventSubInfo> events = TwitchEventSubAPI.getSubscribedEvent(null, null);
				for (EventSubInfo event : events){
					int resultCode = TwitchEventSubAPI.deleteSubscribedEvent(event);
					if(resultCode==204)
						Globals.log.info("Event unsubscribed: "+event);
					else{
						Globals.log.error("Failed to unsubscribe event: "+event+". Response code: "+resultCode);
					}

				}
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

		for (Channel channel : Channels.getChannels().values()){
			HttpHandler handler = channel.httpContext.getHandler();
			if(handler instanceof ChannelCallback callback){
				callback.clearOldIds();
			}
		}
	}

	@NonNull
	public static HttpContext registerChannel(@NonNull Channel channel){

		server.createContext("/"+channel.channelName+"/callback", new ChannelCallback(channel));
		return server.createContext("/"+channel.channelName, new ChannelHandler(channel));
	}
	public static void unregisterChannel(@NonNull HttpContext channelContext){
		server.removeContext(channelContext);
	}
//https://id.twitch.tv/oauth2/authorize?response_type=code&client_id=cxxcdpgmikulrcqf6wb899qxgfgrkw&redirect_uri=http://localhost&scope=viewing_activity_read%20channel:read:subscriptions%20channel:moderate&state=c3ab8aa609ea11e793ae92361f002671'

	static class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			/*String twitchResponse = t.getRequestURI().toString();
			String code = twitchResponse.substring(twitchResponse.indexOf("code=")+5, twitchResponse.indexOf("&"));*/

			//POST https://id.twitch.tv/oauth2/token
			//    ?client_id=<your client ID>
			//    &client_secret=<your client secret>
			//    &code=<authorization code received above>
			//    &grant_type=authorization_code
			//    &redirect_uri=<your registered redirect URI>

			//String response = "This is the response "+code;
			t.sendResponseHeaders(200, 0/*response.length()*/);
			OutputStream os = t.getResponseBody();
			//os.write(response.getBytes());
			os.close();
		}
	}
}
