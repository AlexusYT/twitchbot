package ru.alexus.twitchbot.bot;

import ru.alexus.twitchbot.shared.Channel;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;

public class TwitchUser {
	private final HashMap<EnumBadgeType, BadgeInfo> badges = new HashMap<>();
	private final LinkedList<Integer> emoteSets = new LinkedList<>();
	private Color nickColor;
	private String displayName;
	private String userType, badgeInfo;
	private int subMonths;
	private int userId;
	private boolean turbo, mod, subscriber;

	public TwitchUser(String msgTags){
		for (String tag : msgTags.split(";")) {
			String[] tagElem = tag.split("=");
			try{
				switch (tagElem[0]){
					case "badges":
						for (String badge : tagElem[1].split(",")) {
							BadgeInfo badgeInfo = new BadgeInfo(badge);
							badges.put(badgeInfo.type, badgeInfo);
						}
						break;
					case "emote-sets":
						for (String set : tagElem[1].split(",")) {
							emoteSets.add(Integer.parseInt(set));
						}
						break;
					case "color": nickColor = Color.getColor(tagElem[1]); break;
					case "display-name": displayName = tagElem[1]; break;
					case "user-type": userType = tagElem[1]; break;
					case "badge-info":
						badgeInfo = tagElem[1];
						subMonths = Integer.parseInt(badgeInfo.split("/")[1]);
						break;

					case "user-id": userId = Integer.parseInt(tagElem[1]); break;

					case "turbo": turbo = tagElem[1].equals("1"); break;
					case "mod": mod = tagElem[1].equals("1"); break;
					case "subscriber": subscriber = tagElem[1].equals("1"); break;

					default:
						System.out.println("Unknown user tag: "+tag);
				}
			}catch (Exception ignored){}
		}
	}

	public HashMap<EnumBadgeType, BadgeInfo> getBadges() {
		return badges;
	}
	public int getBadgeVersion(EnumBadgeType type){
		try{
			return badges.get(type).version;
		}catch (Exception e){
			return 0;
		}
	}

	public Color getNickColor() {
		return nickColor;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getUserType() {
		return userType;
	}

	public String getBadgeInfo() {
		return badgeInfo;
	}

	public int getUserId() {
		return userId;
	}

	public boolean isTurbo() {
		return turbo;
	}

	public boolean isMod() {
		return mod;
	}

	public boolean isSubscriber() {
		return subscriber;
	}

	public int getSubMonths() {
		return subMonths;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
