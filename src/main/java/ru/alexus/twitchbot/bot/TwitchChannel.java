package ru.alexus.twitchbot.bot;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TwitchChannel {

	private static final double time = 30;
	private final HashMap<String, TwitchUser> userByName = new HashMap<>();
	private final HashMap<Integer, TwitchUser> userById = new HashMap<>();
	TwitchUser botUser;
	private double cooldown = 1000;
	IChannelEvents listener;
	private boolean emoteOnly;
	private int followersOnly;
	private int channelId;
	private int rituals;
	private boolean slowMode;
	private boolean subsOnly;
	private boolean r9k;
	private final String channelName;
	private TwitchBot bot;
	private long lastSend = -1;


	public TwitchChannel(String channelName, IChannelEvents listener, TwitchBot bot) {
		this.channelName = channelName;
		this.listener = listener;
		this.bot = bot;
	}

	public void setBotUser(@NotNull TwitchUser botUser) {
		if(!botUser.isMod()&&!botUser.isVip()&&!botUser.isBroadcaster()) cooldown = 1000;
		else cooldown = time/100*1000;
		this.botUser = botUser;
	}

	public void initRoom(@NotNull String str){
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

	public synchronized void updateUser(@NotNull TwitchUser newUser){
		TwitchUser old = userById.remove(newUser.getUserId());
		if (old == null) old = new TwitchUser(newUser);
		else {
			userByName.remove(old.getLogin());
			old.copyFrom(newUser);
		}
		userById.put(old.getUserId(), old);
		userByName.put(old.getLogin(), old);

	}

	public void sendMessage(String message){
		try {
			message = listener.onSendingMessage(this, message);
			if(message==null||message.isEmpty()) return;
			while (System.currentTimeMillis()-lastSend<cooldown) {
				TimeUnit.MILLISECONDS.sleep(50);
			}
			bot.sendToIRC("PRIVMSG #"+channelName+" :"+message);
			lastSend = System.currentTimeMillis();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}


	public TwitchUser getBotUser() {
		return botUser;
	}

	public String getChannelName() {
		return channelName;
	}

	public HashMap<String, TwitchUser> getUsersByName() {
		return userByName;
	}

	public HashMap<Integer, TwitchUser> getUsersById() {
		return userById;
	}

	public IChannelEvents getListener() {
		return listener;
	}

	public boolean isEmoteOnly() {
		return emoteOnly;
	}

	public int getFollowersOnly() {
		return followersOnly;
	}

	public int getChannelId() {
		return channelId;
	}

	public int getRituals() {
		return rituals;
	}

	public boolean isSlowMode() {
		return slowMode;
	}

	public boolean isSubsOnly() {
		return subsOnly;
	}

	public boolean isR9k() {
		return r9k;
	}
}
