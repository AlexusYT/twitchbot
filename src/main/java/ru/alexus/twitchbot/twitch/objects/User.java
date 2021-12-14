package ru.alexus.twitchbot.twitch.objects;

import ru.alexus.twitchbot.twitch.Channel;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class User {
	private final HashMap<EnumBadgeType, BadgeInfo> badges = new HashMap<>();
	private Color nickColor;
	private String displayName;
	private String userType, badgeInfo;
	private int subMonths;
	private int userId;
	private boolean turbo, mod, subscriber;
	private int buggycoins = 0;
	public int messagesInSession = -1;
	private boolean mutableByOthers = false;
	private EnumAccessLevel level = EnumAccessLevel.REGULAR;


	public User(String msgTags, Channel channel){
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
					case "color": nickColor = Color.getColor(tagElem[1]); break;
					case "display-name": displayName = tagElem[1]; break;
					case "user-type": userType = tagElem[1]; break;
					case "@badge-info":
						badgeInfo = tagElem[1];
						subMonths = Integer.parseInt(badgeInfo.split("/")[1]);
						System.out.println(subMonths);
						break;

					case "user-id": userId = Integer.parseInt(tagElem[1]); break;

					case "turbo": turbo = tagElem[1].equals("1"); break;
					case "mod": mod = tagElem[1].equals("1"); break;
					case "subscriber": subscriber = tagElem[1].equals("1"); break;

					default:
						System.out.println("Unknown tag: "+tag);
				}
			}catch (Exception ignored){}
		}


		if(userId == 134945794)//Alexus_XX
			level = EnumAccessLevel.OWNER;
		else if(getBadgeVersion(EnumBadgeType.BROADCASTER)!=0)
			level = EnumAccessLevel.BROADCASTER;
		else if(getBadgeVersion(EnumBadgeType.MODERATOR)!=0){
			level = EnumAccessLevel.MODER;
		}else if(getBadgeVersion(EnumBadgeType.SUBSCRIBER)!=0)
			level = EnumAccessLevel.SUBSCRIBER;
		else{
			level = EnumAccessLevel.REGULAR;
		}
		User user = channel.getUserById(userId);
		if(user==null) {
			return;
		}
		this.buggycoins = user.buggycoins;
		this.messagesInSession = user.messagesInSession;
		this.mutableByOthers = user.mutableByOthers;
		channel.setUserById(user.getUserId(), user);


	}

	public User(ResultSet resultSet) throws SQLException {
		buggycoins = resultSet.getInt("buggycoins");
		userId = resultSet.getInt("twitchID");
		displayName = resultSet.getString("nickname");
		mutableByOthers = resultSet.getBoolean("mutable");
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

	public EnumAccessLevel getLevel() {
		return level;
	}

	public int getBuggycoins() {
		return buggycoins;
	}

	public void addBuggycoins(int buggycoins) {
		this.buggycoins += buggycoins;
	}
	public void setBuggycoins(int buggycoins) {
		this.buggycoins = buggycoins;
	}

	public int getSubMonths() {
		return subMonths;
	}

	public boolean isMutableByOthers() {
		return mutableByOthers;
	}

	public void setMutableByOthers(boolean mutableByOthers) {
		this.mutableByOthers = mutableByOthers;
	}

	@Override
	public String toString() {
		return "User{" + "displayName='" + displayName + '\'' +
				", userId=" + userId +
				", buggycoins=" + buggycoins +
				", messagesInSession=" + messagesInSession +
				", level=" + level +
				'}';
	}
}
