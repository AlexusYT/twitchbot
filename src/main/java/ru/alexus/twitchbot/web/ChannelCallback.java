package ru.alexus.twitchbot.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.shared.Channel;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
		try {
			if (messageType != null) {
				if (messageType.equals("webhook_callback_verification")) {
					Globals.log.info("clientBody: " + clientBody);
					JSONObject body = new JSONObject(clientBody);
					Globals.log.info("body: " + body);
					String challenge = body.getString("challenge");
					Globals.log.info("Challenge: " + challenge);

					t.sendResponseHeaders(200, challenge.length());
					OutputStream os = t.getResponseBody();
					os.write(challenge.getBytes(StandardCharsets.UTF_8));
					os.close();
					return;
				}
			}
		}catch (Exception e){
			Globals.log.error("Error occurred", e);
		}

		String ret = "test";
		t.sendResponseHeaders(200, ret.length());
		OutputStream os = t.getResponseBody();
		os.write(ret.getBytes(StandardCharsets.UTF_8));
		os.close();
	}
}
