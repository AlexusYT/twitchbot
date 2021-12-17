package ru.alexus.twitchbot.bot;

import ru.alexus.twitchbot.bot.EnumBadgeType;

public class BadgeInfo {
	public EnumBadgeType type;
	public int version;
	public BadgeInfo(String badge){
		String[] str = badge.split("/");
		switch (str[0]) {
			case "admin" -> type = EnumBadgeType.ADMIN;
			case "bits" -> type = EnumBadgeType.BITS;
			case "broadcaster" -> type = EnumBadgeType.BROADCASTER;
			case "global_mod" -> type = EnumBadgeType.GLOBAL_MOD;
			case "moderator" -> type = EnumBadgeType.MODERATOR;
			case "subscriber" -> type = EnumBadgeType.SUBSCRIBER;
			case "staff" -> type = EnumBadgeType.STAFF;
			case "turbo" -> type = EnumBadgeType.TURBO;
		}
		version = Integer.parseInt(str[1]);
	}
}
