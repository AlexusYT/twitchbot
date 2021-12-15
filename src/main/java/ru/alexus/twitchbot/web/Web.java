package ru.alexus.twitchbot.web;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.springframework.http.HttpEntity;
import org.springframework.lang.NonNull;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.shared.Channel;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Web {
	static HttpServer server;
	private static LinkedHashMap<String, HttpContext> channelContexts;

	public static void startWeb(){
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
	@NonNull
	public static HttpContext registerChannel(@NonNull Channel channel){
		server.createContext("/"+channel.channelName+"/callback", new ChannelCallback(channel));
		return server.createContext("/"+channel.channelName, new ChannelHandler(channel));
	}
	public static void unregisterChannel(@NonNull HttpContext channelContext){
		server.removeContext(channelContext);
	}
//https://id.twitch.tv/oauth2/authorize?response_type=code&client_id=cxxcdpgmikulrcqf6wb899qxgfgrkw&redirect_uri=http://localhost&scope=viewing_activity_read%20channel:read:subscriptions&state=c3ab8aa609ea11e793ae92361f002671'

	static class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String twitchResponse = t.getRequestURI().toString();
			String code = twitchResponse.substring(twitchResponse.indexOf("code=")+5, twitchResponse.indexOf("&"));

			String response = Utils.sendPost("https://id.twitch.tv/oauth2/token", null, "client_id="+Globals.twitchClientId +
					"&client_secret="+Globals.twitchSecret +
					"&code="+code +
					"&grant_type=authorization_code" +
					"&redirect_uri=http://localhost");
			//POST https://id.twitch.tv/oauth2/token
			//    ?client_id=<your client ID>
			//    &client_secret=<your client secret>
			//    &code=<authorization code received above>
			//    &grant_type=authorization_code
			//    &redirect_uri=<your registered redirect URI>

			//String response = "This is the response "+code;
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
