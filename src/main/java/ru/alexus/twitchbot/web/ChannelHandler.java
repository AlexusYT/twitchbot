package ru.alexus.twitchbot.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.shared.ChannelOld;

import java.io.*;

class ChannelHandler implements HttpHandler {
	String index;
	ChannelOld channel;

	public ChannelHandler(ChannelOld channel) {
		this.channel = channel;
		try {
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream is = classloader.getResourceAsStream("html/channel/index.html");
			if (is == null) {
				index = "Template not found";
				return;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				builder.append(line.replaceAll("\\{channelName}", channel.channelName)).append("\n");
			}
			index = builder.toString();
		} catch (Exception e) {
			Globals.log.error("Failed to setup template for channel " + channel.channelName, e);
			index = "Failed to setup template for channel " + channel.channelName;
		}

	}

	/*
	curl -X POST 'https://api.twitch.tv/helix/eventsub/subscriptions' \
	-H 'Authorization: Bearer 2gbdx6oar67tqtcmt49t3wpcgycthx' \
	-H 'Client-Id: wbmytr93xzw8zbg0p1izqyzzc5mbiz' \
	-H 'Content-Type: application/json' \
	-d '{"type":"user.update","version":"1","condition":{"user_id":"1234"},"transport":{"method":"webhook","callback":"https://this-is-a-callback.com","secret":"s3cre7"}}'
	 */
	@Override
	public void handle(HttpExchange t) throws IOException {


		//HttpPost httppost = new HttpPost("https://api.twitch.tv/helix/eventsub/subscriptions");

		t.sendResponseHeaders(200, index.length());
		OutputStream os = t.getResponseBody();
		os.write(index.getBytes());
		os.close();

	}
}
