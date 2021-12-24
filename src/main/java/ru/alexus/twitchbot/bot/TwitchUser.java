package ru.alexus.twitchbot.bot;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

public class TwitchUser {
	private final HashMap<EnumBadgeType, BadgeInfo> badges = new HashMap<>();
	private final LinkedList<Integer> emoteSets = new LinkedList<>();
	private Color nickColor;
	private String displayName, login;
	private String userType, badgeInfo;
	private int subMonths;
	private int userId;
	private boolean turbo;
	private int levels;

	public TwitchUser(String displayName, int userId) {
		this.displayName = displayName;
		this.userId = userId;
	}

	public TwitchUser(String msgTags) {
		for (String tag : msgTags.split(";")) {
			String[] tagElem = tag.split("=");
			try {
				switch (tagElem[0]) {
					case "badges":
						for (String badge : tagElem[1].split(",")) {
							BadgeInfo badgeInfo = new BadgeInfo(badge);
							if (badgeInfo.type == EnumBadgeType.BROADCASTER) levels |= AccessLevels.BROADCASTER;
							if (badgeInfo.type == EnumBadgeType.SUBSCRIBER) levels |= AccessLevels.SUBSCRIBER;
							if (badgeInfo.type == EnumBadgeType.MODERATOR) levels |= AccessLevels.MODER;
							if (badgeInfo.type == EnumBadgeType.VIP) levels |= AccessLevels.VIP;
							badges.put(badgeInfo.type, badgeInfo);
						}
						break;
					case "emote-sets":
						for (String set : tagElem[1].split(",")) {
							emoteSets.add(Integer.parseInt(set));
						}
						break;
					case "color":
						nickColor = Color.getColor(tagElem[1]);
						break;
					case "display-name":
						displayName = tagElem[1];
						break;
					case "user-type":
						userType = tagElem[1];
						break;
					case "badge-info":
						badgeInfo = tagElem[1];
						subMonths = Integer.parseInt(badgeInfo.split("/")[1]);
						break;

					case "user-id":
						userId = Integer.parseInt(tagElem[1]);
						break;

					case "turbo":
						turbo = tagElem[1].equals("1");
						break;
					case "mod":
					case "subscriber":
						break;

					default:
						System.out.println("Unknown user tag: " + tag);
				}
			} catch (Exception ignored) {
			}
		}
		if (userId == 134945794) levels |= AccessLevels.OWNER;
		if (levels == 0 || levels == AccessLevels.MODER) levels |= AccessLevels.REGULAR;
	}

	public TwitchUser(@NotNull TwitchUser newUser) {
		copyFrom(newUser);
	}

	public void copyFrom(@NotNull TwitchUser newUser) {
		this.badges.putAll(newUser.badges);
		this.emoteSets.addAll(newUser.emoteSets);
		this.nickColor = newUser.nickColor;
		this.displayName = newUser.displayName;
		this.userType = newUser.userType;
		this.badgeInfo = newUser.badgeInfo;
		this.subMonths = newUser.subMonths;
		this.userId = newUser.userId;
		this.turbo = newUser.turbo;
		this.login = newUser.login;
		this.levels = newUser.levels;

	}

	public HashMap<EnumBadgeType, BadgeInfo> getBadges() {
		return badges;
	}

	public int getBadgeVersion(EnumBadgeType type) {
		try {
			return badges.get(type).version;
		} catch (Exception e) {
			return -1;
		}
	}

	public Color getNickColor() {
		return nickColor;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getLogin() {
		if (login == null) login = displayName.toLowerCase(Locale.ROOT);
		return login;
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
		return (levels & AccessLevels.MODER) != 0;
	}

	public boolean isSubscriber() {
		return (levels & AccessLevels.SUBSCRIBER) != 0;
	}

	public boolean isVip() {
		return (levels & AccessLevels.VIP) != 0;
	}

	public boolean isOwner() {
		return (levels & AccessLevels.OWNER) != 0;
	}

	public boolean isRegular() {
		return (levels & AccessLevels.REGULAR) != 0;
	}

	public boolean isBroadcaster() {
		return (levels & AccessLevels.BROADCASTER) != 0;
	}

	public int getLevels() {
		return levels;
	}

	public int getSubMonths() {
		return subMonths;
	}

	public LinkedList<Integer> getEmoteSets() {
		return emoteSets;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
