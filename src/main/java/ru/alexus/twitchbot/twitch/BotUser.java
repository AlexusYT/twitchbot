package ru.alexus.twitchbot.twitch;

import org.springframework.lang.NonNull;
import ru.alexus.twitchbot.bot.TwitchUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class BotUser {
	private TwitchUser twitchUser;
	private int coins, messages=-1;
	private boolean mutable;

	public final HashMap<CommandInfo, Long> commandsLastExecute = new HashMap<>();
	public final HashMap<CommandInfo, Integer> commandsExecuteCount = new HashMap<>();

	public BotUser(TwitchUser twitchUser, ResultSet set){
		try {
			if (twitchUser != null) this.twitchUser = twitchUser;
			else {
				if(set==null) return;
				this.twitchUser = new TwitchUser(set.getString("nickname"), set.getInt("twitchID"));
			}
			if(set==null) return;
			coins = set.getInt("buggycoins");
			mutable = set.getBoolean("mutable");

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public String getLogin(){
		return twitchUser.getLogin();
	}

	public String getDisplayName(){
		return twitchUser.getDisplayName();
	}

	public void copyFrom(TwitchUser newUser) {
		this.twitchUser.copyFrom(newUser);
	}

	public TwitchUser getTwitchUser() {
		return twitchUser;
	}

	public int getUserId() {
		return twitchUser.getUserId();
	}

	public int getCoins() {
		return coins;
	}

	public void setCoins(int coins) {
		this.coins = coins;
	}

	public void addCoins(int coins) {
		this.coins += coins;
	}

	public void removeCoins(int coins) {
		this.coins -= coins;
	}


	@NonNull
	public CommandResult checkSufficientCoins(@NonNull CommandInfo info){

		CommandResult result = new CommandResult();
		result.coinCost = info.executor.getCoinCost(this);
		result.sufficientCoins = this.getCoins() - result.coinCost >=0;
		return result;
	}


	public boolean isSubscriber(){
		return twitchUser.isSubscriber();
	}

	public boolean isBroadcaster(){
		return twitchUser.isBroadcaster();
	}

	public boolean isMod() {
		return twitchUser.isMod();
	}

	public boolean isVip() {
		return twitchUser.isVip();
	}

	public boolean isOwner() {
		return twitchUser.isOwner();
	}

	public boolean isRegular(){
		return twitchUser.isRegular();
	}

	public int getMessagesInSession() {
		return messages;
	}

	public void incMessagesInSession() {
		this.messages++;
	}

	public void setMessagesInSession(int messages) {
		this.messages = messages;
	}

	@Override
	public String toString() {
		return "BotUser{" +
				"twitchUser=" + twitchUser +
				", coins=" + coins +
				", mutable=" + mutable +
				", messages=" + messages +
				'}';
	}

	public boolean isMutable() {
		return mutable;
	}

	public void setMutable(boolean mutable) {
		this.mutable = mutable;
	}

}
