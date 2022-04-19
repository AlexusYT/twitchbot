package ru.alexus.twitchbot.twitch;

import java.util.LinkedHashMap;

public class BotConfig {
	public static final LinkedHashMap<String, BotChannel> botChannels = new LinkedHashMap<>();

	public static BotChannel getChannel(String name){
		botChannels.putIfAbsent(name, new BotChannel(name));
		return botChannels.get(name);
	}

}
