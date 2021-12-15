package ru.alexus.twitchbot.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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
		System.out.println("Method: "+t.getRequestMethod());
		BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			builder.append(line).append("\n");
		}
		System.out.println("Body: "+builder);
		System.out.println("URI: "+t.getRequestURI().toString());
		StringBuilder headers = new StringBuilder();

		for(Map.Entry<String, List<String>> header : t.getRequestHeaders().entrySet()){
			headers.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
		}
		System.out.print("Headers: "+headers);
		String ret = "test";
		t.sendResponseHeaders(200, ret.length());
		OutputStream os = t.getResponseBody();
		os.write(ret.getBytes(StandardCharsets.UTF_8));
		os.close();
	}
}
