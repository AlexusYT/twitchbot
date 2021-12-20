package ru.alexus.twitchbot.bot;

import java.util.Calendar;

public class TwitchMessage {
	private String emotes;
	private String msgId;
	private String clientNonce;
	private String flags;
	private final String text;
	private int roomId;
	private boolean firstMsg, emoteOnly;
	private Calendar sendTime;
	private String replyToUserDisplayName;
	private String replyToMsgBody;
	private String replyToMsgId;
	private int replyToUserId = -1;
	private String replyToUserLogin;
	private final TwitchUser twitchUser;

	public TwitchMessage(String msgTags, String text){
		this.text = text;
		StringBuilder builder = new StringBuilder();
		for (String tag : msgTags.split(";")) {
			String[] tagElem = tag.split("=");
			try{
				switch (tagElem[0]) {
					case "emotes" -> emotes = tagElem[1];
					case "id" -> msgId = tagElem[1];
					case "client-nonce" -> clientNonce = tagElem[1];
					case "flags" -> flags = tagElem[1];
					case "reply-parent-display-name" -> replyToUserDisplayName = tagElem[1];
					case "reply-parent-msg-body" -> replyToMsgBody = tagElem[1];
					case "reply-parent-msg-id" -> replyToMsgId = tagElem[1];
					case "reply-parent-user-login" -> replyToUserLogin = tagElem[1];
					case "reply-parent-user-id" -> replyToUserId = Integer.parseInt(tagElem[1]);
					case "room-id" -> roomId = Integer.parseInt(tagElem[1]);
					case "tmi-sent-ts" -> {
						sendTime = Calendar.getInstance();
						sendTime.setTimeInMillis(Long.parseLong(tagElem[1]));
					}
					case "first-msg" -> firstMsg = tagElem[1].equals("1");
					case "emote-only" -> emoteOnly = tagElem[1].equals("1");
					default -> builder.append(tag).append(";");
				}
			}catch (Exception ignored){}
		}

		this.twitchUser = new TwitchUser(builder.toString());

	}


	public String getEmotes() {
		return emotes;
	}

	public String getMsgId() {
		return msgId;
	}

	public String getClientNonce() {
		return clientNonce;
	}

	public String getFlags() {
		return flags;
	}

	public int getRoomId() {
		return roomId;
	}

	public boolean isFirstMsg() {
		return firstMsg;
	}

	public Calendar getSendTime() {
		return sendTime;
	}

	public String getText() {
		return text;
	}

	public boolean isReplying() {
		return replyToUserId!=-1;
	}

	public String getReplyToUserDisplayName() {
		return replyToUserDisplayName;
	}

	public String getReplyToMsgBody() {
		return replyToMsgBody;
	}

	public String getReplyToMsgId() {
		return replyToMsgId;
	}

	public int getReplyToUserId() {
		return replyToUserId;
	}

	public String getReplyToUserLogin() {
		return replyToUserLogin;
	}

	public TwitchUser getTwitchUser() {
		return twitchUser;
	}

	public boolean isEmoteOnly() {
		return emoteOnly;
	}

	@Override
	public String toString() {
		return text;
	}
}
