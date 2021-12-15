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
import java.util.List;
import java.util.Map;

class ChannelCallback implements HttpHandler {
	Channel channel;

	public ChannelCallback(Channel channel){
		this.channel = channel;

	}

	@Override
	public void handle(HttpExchange t) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
		StringBuilder clientBody = new StringBuilder();
		String line;
		String __delim = "";
		while ((line = br.readLine()) != null) {
			clientBody.append(__delim).append(line);
			__delim = "\n";
		}
		Headers requestHeaders = t.getRequestHeaders();

		Globals.log.info("Method: "+t.getRequestMethod());
		Globals.log.info("URI: "+t.getRequestURI().toString());
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
			Globals.log.error("HMAC mismatch for event "+subscriptionType+" for channel "+channel.channelName);
			respond(t, 400);
			return;
		}

		try {
			if (messageType.equals("webhook_callback_verification")) {
				JSONObject body = new JSONObject(clientBody.toString());
				String challenge = body.getString("challenge");

				respond(t, 200, challenge);
				return;
			}
		}catch (Exception e){
			Globals.log.error("Error occurred", e);
			respond(t, 400);
			return;
		}

		respond(t, 200);
	}

	private void respond(HttpExchange exchange, int code) throws IOException {
		respond(exchange, code, null);
	}

	private void respond(@NonNull HttpExchange exchange, int code, String body) throws IOException {
		OutputStream os = exchange.getResponseBody();
		if(body==null){
			exchange.sendResponseHeaders(code, 0);
		}else{
			exchange.sendResponseHeaders(code, body.length());
			os.write(body.getBytes(StandardCharsets.UTF_8));
		}
		os.close();
	}
}
