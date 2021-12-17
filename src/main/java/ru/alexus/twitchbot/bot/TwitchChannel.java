package ru.alexus.twitchbot.bot;

import java.util.HashMap;

public class TwitchChannel {

	private final HashMap<String, TwitchChannel> userByName = new HashMap<>();
	private final HashMap<Integer, TwitchChannel> userById = new HashMap<>();
	TwitchUser botUser;
	IChannelEvents listener;
	private boolean emoteOnly;
	private int followersOnly;
	private int channelId;
	private int rituals;
	private boolean slowMode;
	private boolean subsOnly;
	private boolean r9k;
	private final String channelName;


	public TwitchChannel(String channelName, IChannelEvents listener) {
		this.channelName = channelName;
		this.listener = listener;
	}

	public void setBotUser(TwitchUser botUser) {
		this.botUser = botUser;
	}

	public void initRoom(String str){
		for (String tag : str.split(";")) {
			String[] tagElem = tag.split("=");
			try{
				switch (tagElem[0]) {
					case "emote-only" -> emoteOnly = tagElem[1].equals("1");
					case "followers-only" -> followersOnly = Integer.parseInt(tagElem[1]);
					case "room-id" -> channelId = Integer.parseInt(tagElem[1]);
					case "subs-only" -> subsOnly = tagElem[1].equals("1");
					case "slow" -> slowMode = tagElem[1].equals("1");
					case "r9k" -> r9k = tagElem[1].equals("1");
					case "rituals" -> rituals = Integer.parseInt(tagElem[1]);
					default -> System.out.println("Unknown channel tag: " + tag);
				}
			}catch (Exception ignored){}
		}
	}

	public TwitchUser getBotUser() {
		return botUser;
	}

	public String getChannelName() {
		return channelName;
	}

}
