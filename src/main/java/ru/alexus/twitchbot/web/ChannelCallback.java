package ru.alexus.twitchbot.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.shared.Channel;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ChannelCallback implements HttpHandler {
	Channel channel;
	LinkedHashMap<String, Long> ids = new LinkedHashMap<>();
	long packets = 0;
	long last = System.currentTimeMillis();


	public ChannelCallback(Channel channel){
		this.channel = channel;

	}

	public void clearOldIds(){

		for(Map.Entry<String, Long> id : ids.entrySet()){
			if(id.getValue()+600000<System.currentTimeMillis()) ids.remove(id.getKey());
		}

	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		long lastTime = System.currentTimeMillis();
		BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
		StringBuilder clientBody = new StringBuilder();
		String line;
		String __delim = "";
		while ((line = br.readLine()) != null) {
			clientBody.append(__delim).append(line);
			__delim = "\n";
		}
		Headers requestHeaders = t.getRequestHeaders();
/*
		Globals.log.info("Method: "+t.getRequestMethod());
		Globals.log.info("URI: "+t.getRequestURI().toString());
		StringBuilder headers = new StringBuilder();
		for(Map.Entry<String, List<String>> header : requestHeaders.entrySet()){
			headers.append(header.getKey()).append(": ").append(header.getValue().get(0)).append("\n");
		}
		Globals.log.info("Headers: "+headers);
		Globals.log.info("Body: "+clientBody);*/


		String messageType = requestHeaders.getFirst("Twitch-Eventsub-Message-Type");
		String messageId = requestHeaders.getFirst("Twitch-Eventsub-Message-Id");
		String messageHMAC = requestHeaders.getFirst("Twitch-Eventsub-Message-Signature");
		String subscriptionType = requestHeaders.getFirst("Twitch-Eventsub-Subscription-Type");
		String messageTimestamp = requestHeaders.getFirst("Twitch-Eventsub-Message-Timestamp");
		//String subscriptionVersion = requestHeaders.getFirst("Twitch-Eventsub-Subscription-Version");
		if(messageHMAC==null||messageType==null||messageId==null||subscriptionType==null||messageTimestamp==null) {
			Globals.log.error("One of important headers is null");
			respond(t, 400);
			return;
		}

		EventSubInfo subInfo = channel.subscriptions.get(subscriptionType);

		if(subInfo==null) {
			Globals.log.error("Event not found "+subscriptionType+" for channel "+channel.channelName);
			respond(t, 400);
			return;
		}

		if(!messageHMAC.equals("sha256="+Utils.hmacSha256(messageId+messageTimestamp+clientBody, subInfo.getSecret()))){
			//Globals.log.error("HMAC mismatch for event "+subscriptionType+" for channel "+channel.channelName);
			respond(t, 400);
			return;
		}

		if(ids.get(messageId)!=null){
			//Globals.log.error("Notification duplicate detected");
			respond(t, 200);
			return;
		}

		JSONObject object;
		try{
			object = new JSONObject(clientBody.toString());
		}catch (Exception ignored){
			object = new JSONObject();
		}
		switch (messageType) {
			case "webhook_callback_verification":
				respond(t, 200, object.optString("challenge", null));
				break;
			case "notification":
				if (object.has("subscription") && object.has("event")) {
					JSONObject subscription = object.getJSONObject("subscription");
					JSONObject event = object.getJSONObject("event");
					subInfo.setStatus(subscription.getString("status"));
					new Thread(() -> channel.subscriptionNotification(subInfo, event), channel.channelName+"/"+subInfo.getType()).start();
					respond(t, 200);
				}
				break;
			case "revocation":
				if (object.has("subscription")) {
					JSONObject subscription = object.getJSONObject("subscription");
					subInfo.setStatus(subscription.getString("status"));
					new Thread(() -> channel.subscriptionRevoked(subInfo), channel.channelName+"/"+subInfo.getType()).start();
					respond(t, 200);
				}
				break;
			default:
				respond(t, 401);

				break;
		}
		channel.subscriptions.put(subscriptionType, subInfo);
		ids.put(messageId, System.currentTimeMillis());
		if(System.currentTimeMillis()-lastTime>5){

			Globals.log.info(messageId+" "+(System.currentTimeMillis()-lastTime));
		}
		if(System.currentTimeMillis()-last>1000){
			System.out.println(packets+" p/s");
			packets = 0;
			last = System.currentTimeMillis();

		}
		packets++;
	}

	private void respond(HttpExchange exchange, int code) {
		respond(exchange, code, null);
	}

	private void respond(@NonNull HttpExchange exchange, int code, String body){

		try {
			OutputStream os = exchange.getResponseBody();
			if (body == null) {
				exchange.sendResponseHeaders(code, 0);
			} else {
				exchange.sendResponseHeaders(code, body.length());
				os.write(body.getBytes(StandardCharsets.UTF_8));
			}
			os.close();
		}catch (Exception e){
			Globals.log.error("Error occurred", e);
		}
	}
}
