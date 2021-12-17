package ru.alexus.twitchbot.bot;

public class TwitchWhisper {
	private String emotes;
	private int msgId;
	private String threadId;
	private final String text;
	private final TwitchUser twitchUser;

	public TwitchWhisper(String msgTags, String text){
		this.text = text;
		StringBuilder builder = new StringBuilder();
		for (String tag : msgTags.split(";")) {
			String[] tagElem = tag.split("=");
			try{
				switch (tagElem[0]) {
					case "emotes" -> emotes = tagElem[1];
					case "message-id" -> msgId = Integer.parseInt(tagElem[1]);
					case "thread-id" -> threadId = tagElem[1];
					default -> builder.append(tag).append(";");
				}
			}catch (Exception ignored){}
		}

		this.twitchUser = new TwitchUser(builder.toString());

	}


	public String getEmotes() {
		return emotes;
	}

	public int getMsgId() {
		return msgId;
	}

	public String getText() {
		return text;
	}

	public TwitchUser getTwitchUser() {
		return twitchUser;
	}

	public String getThreadId() {
		return threadId;
	}
}
