package ru.alexus.twitchbot.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.events.Event;
import ru.alexus.twitchbot.eventsub.events.RedemptionAdd;
import ru.alexus.twitchbot.eventsub.events.StreamOnline;
import ru.alexus.twitchbot.shared.ChannelOld;
import ru.alexus.twitchbot.twitch.BotChannel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ChannelCallback implements HttpHandler {
	private final BotChannel channel;
	LinkedHashMap<String, Long> ids = new LinkedHashMap<>();
	long packets = 0;
	long last = System.currentTimeMillis();
	private final IEventSub sub;

	public ChannelCallback(BotChannel channel){
		this.channel = channel;
		this.sub = channel;

	}

	public void clearOldIds(){

		for(Map.Entry<String, Long> id : ids.entrySet()){
			if(id.getValue()+600000<System.currentTimeMillis()) ids.remove(id.getKey());
		}

	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		long lastTime = System.currentTimeMillis();
		Headers requestHeaders = exchange.getRequestHeaders();
		String clientBody = getRequestBody(exchange);


		Globals.log.info("Method: "+exchange.getRequestMethod());
		Globals.log.info("URI: "+exchange.getRequestURI().toString());
		StringBuilder headers = new StringBuilder();
		for(Map.Entry<String, List<String>> header : requestHeaders.entrySet()){
			headers.append(header.getKey()).append(": ").append(header.getValue().get(0)).append("\n");
		}
		Globals.log.info("Headers: "+headers);
		Globals.log.info("Body: "+clientBody);
		String messageType = requestHeaders.getFirst("Twitch-Eventsub-Message-Type");
		String messageId = requestHeaders.getFirst("Twitch-Eventsub-Message-Id");
		String messageHMAC = requestHeaders.getFirst("Twitch-Eventsub-Message-Signature");
		String subscriptionType = requestHeaders.getFirst("Twitch-Eventsub-Subscription-Type");
		String messageTimestamp = requestHeaders.getFirst("Twitch-Eventsub-Message-Timestamp");
		//String subscriptionVersion = requestHeaders.getFirst("Twitch-Eventsub-Subscription-Version");
		if(messageHMAC==null||messageType==null||messageId==null||subscriptionType==null||messageTimestamp==null) {
			Globals.log.error("One of important headers is null");
			respond(exchange, 400);
			return;
		}

		EventSubInfo subInfo = channel.getEvent(subscriptionType);

		if(subInfo==null) {
			Globals.log.error("Event not found "+subscriptionType+" for channel "+channel.getName());
			respond(exchange, 400);
			return;
		}
		String hmac = Utils.hmacSha256(messageId+messageTimestamp+clientBody, subInfo.getSecret());
		if(!messageHMAC.equals("sha256="+hmac)){
			Globals.log.error("HMAC mismatch for event "+subscriptionType+" for channel "+channel.getName());
			respond(exchange, 400);
			return;
		}

		if(ids.get(messageId)!=null){
			Globals.log.error("Notification duplicate detected");
			respond(exchange, 200);
			return;
		}

		JSONObject object;
		try{
			object = new JSONObject(clientBody);
		}catch (Exception ignored){
			object = new JSONObject();
		}
		switch (messageType) {
			case "webhook_callback_verification":
				respond(exchange, 200, object.optString("challenge", null));
				break;
			case "notification":
				if (object.has("subscription") && object.has("event")) {
					JSONObject subscription = object.getJSONObject("subscription");
					JSONObject eventObj = object.getJSONObject("event");
					subInfo.setStatus(subscription.getString("status"));
					new Thread(() ->{
						switch (subInfo.getType()) {
							case "channel.channel_points_custom_reward_redemption.add" -> channel.onRewardRedemption(subInfo, new RedemptionAdd(eventObj));
							case "stream.online" -> channel.onStreamOnline(subInfo, new StreamOnline(eventObj));
							case "stream.offline" -> channel.onStreamOffline(subInfo, new Event(eventObj));
						}
						//else
						//channel.subscriptionNotification(subInfo, eventObj);
					}, channel.getName()+"/"+subInfo.getType()).start();
					respond(exchange, 200);
				}
				break;
			case "revocation":
				if (object.has("subscription")) {
					JSONObject subscription = object.getJSONObject("subscription");
					subInfo.setStatus(subscription.getString("status"));
					new Thread(() -> channel.subscriptionRevoked(subInfo), channel.getName()+"/"+subInfo.getType()).start();
					respond(exchange, 200);
				}
				break;
			default:
				respond(exchange, 401);

				break;
		}

		ids.put(messageId, System.currentTimeMillis());
		if(System.currentTimeMillis()-lastTime>5){

			Globals.log.info("Message "+messageId+" took "+(System.currentTimeMillis()-lastTime));
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

	String getRequestBody(HttpExchange exchange){
		StringBuilder clientBody = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
			String line;
			String __delim = "";
			while ((line = br.readLine()) != null) {
				clientBody.append(__delim).append(line);
				__delim = "\n";
			}
		}catch (Exception ignored){}
		return clientBody.toString();
	}
}
