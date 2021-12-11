package ru.alexus.twitchbot.twitch.objects;

public class BadgeInfo {
	EnumBadgeType type;
	int version;
	public BadgeInfo(String badge){
		String[] str = badge.split("/");
		switch (str[0]){
			case "admin": type = EnumBadgeType.ADMIN; break;
			case "bits": type = EnumBadgeType.BITS; break;
			case "broadcaster": type = EnumBadgeType.BROADCASTER; break;
			case "global_mod": type = EnumBadgeType.GLOBAL_MOD; break;
			case "moderator": type = EnumBadgeType.MODERATOR; break;
			case "subscriber": type = EnumBadgeType.SUBSCRIBER; break;
			case "staff": type = EnumBadgeType.STAFF; break;
			case "turbo": type = EnumBadgeType.TURBO; break;
		}
		version = Integer.parseInt(str[1]);
	}
}
