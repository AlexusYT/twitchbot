package ru.alexus.twitchbot.twitch.objects;

import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.Channels;

import java.util.Calendar;

public class MsgTags{
	private final User user;
	private String emotes;
	private String msgId;
	private String clientNonce;
	private String flags;
	private final String channelName;
	private int roomId;
	private boolean firstMsg, emoteOnly;
	private Calendar sendTime;
	private String replyToUserDisplayName;
	private String replyToMsgBody;
	private String replyToMsgId;
	private int replyToUserId = -1;
	private String replyToUserLogin;
	public Channel channel;

	public MsgTags(String msgTags, String channelName){
		this.channelName = channelName;
		StringBuilder builder = new StringBuilder();
		for (String tag : msgTags.split(";")) {
			String[] tagElem = tag.split("=");
			try{
				switch (tagElem[0]){
					case "emotes": emotes = tagElem[1]; break;
					case "id": msgId = tagElem[1]; break;
					case "client-nonce": clientNonce = tagElem[1]; break;
					case "flags": flags = tagElem[1]; break;
					case "reply-parent-display-name": replyToUserDisplayName = tagElem[1]; break;
					case "reply-parent-msg-body": replyToMsgBody = tagElem[1]; break;
					case "reply-parent-msg-id": replyToMsgId = tagElem[1]; break;
					case "reply-parent-user-login": replyToUserLogin = tagElem[1]; break;

					case "reply-parent-user-id": replyToUserId = Integer.parseInt(tagElem[1]); break;
					case "room-id": roomId = Integer.parseInt(tagElem[1]); break;

					case "tmi-sent-ts":
						sendTime = Calendar.getInstance();
						sendTime.setTimeInMillis(Long.parseLong(tagElem[1]));
						break;

					case "first-msg": firstMsg = tagElem[1].equals("1"); break;
					case "emote-only": emoteOnly = tagElem[1].equals("1"); break;

					default:
						builder.append(tag).append(";");
				}
			}catch (Exception ignored){}
		}

		channel = Channels.getChannel(channelName);

		this.user = new User(builder.toString(), channel);

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

	public String getChannelName() {
		return channelName;
	}

	public User getUser() {
		return user;
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
}
