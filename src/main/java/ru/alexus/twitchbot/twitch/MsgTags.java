package ru.alexus.twitchbot.twitch;

import java.awt.*;
import java.util.Calendar;

public class MsgTags {
	private String badges;
	private Color nickColor;
	private String displayName;
	private String emotes;
	private String msgId;
	private String clientNonce;
	private String flags;
	private String userType, badgeInfo;
	private int roomId, userId;
	private boolean turbo, firstMsg, mod, subscriber, emoteOnly;
	private Calendar sendTime;

	MsgTags(String msgTags){
		for (String tag : msgTags.split(";")) {
			String[] tagElem = tag.split("=");
			try{
				switch (tagElem[0]){
					case "badges": badges = tagElem[1]; break;
					case "color": nickColor = Color.getColor(tagElem[1]); break;
					case "display-name": displayName = tagElem[1]; break;
					case "emotes": emotes = tagElem[1]; break;
					case "id": msgId = tagElem[1]; break;
					case "client-nonce": clientNonce = tagElem[1]; break;
					case "flags": flags = tagElem[1]; break;
					case "user-type": userType = tagElem[1]; break;
					case "@badge-info": badgeInfo = tagElem[1]; break;

					case "room-id": roomId = Integer.parseInt(tagElem[1]); break;
					case "user-id": userId = Integer.parseInt(tagElem[1]); break;

					case "tmi-sent-ts":
						sendTime = Calendar.getInstance();
						sendTime.setTimeInMillis(Long.parseLong(tagElem[1]));
						break;

					case "turbo": turbo = tagElem[1].equals("1"); break;
					case "first-msg": firstMsg = tagElem[1].equals("1"); break;
					case "mod": mod = tagElem[1].equals("1"); break;
					case "subscriber": subscriber = tagElem[1].equals("1"); break;
					case "emote-only": emoteOnly = tagElem[1].equals("1"); break;

					default:
						System.out.println("Unknown tag: "+tag);
				}
			}catch (Exception ignored){}
		}


	}

	public String getBadges() {
		return badges;
	}

	public Color getNickColor() {
		return nickColor;
	}

	public String getDisplayName() {
		return displayName;
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

	public String getUserType() {
		return userType;
	}

	public String getBadgeInfo() {
		return badgeInfo;
	}

	public int getRoomId() {
		return roomId;
	}

	public int getUserId() {
		return userId;
	}

	public boolean isTurbo() {
		return turbo;
	}

	public boolean isFirstMsg() {
		return firstMsg;
	}

	public boolean isMod() {
		return mod;
	}

	public boolean isSubscriber() {
		return subscriber;
	}

	public Calendar getSendTime() {
		return sendTime;
	}



}
