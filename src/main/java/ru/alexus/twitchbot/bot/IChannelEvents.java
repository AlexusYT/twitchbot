package ru.alexus.twitchbot.bot;


public interface IChannelEvents {

	void onBotChannelJoin(TwitchChannel channel);
	void onBotChannelLeave(TwitchChannel channel);
	void onBotChannelJoinFailed(TwitchChannel channel, String reason);

	void onUserJoin(TwitchChannel channel, String user);
	void onUserLeft(TwitchChannel channel, String user);

	void onMessage(TwitchChannel channel, TwitchUser user, TwitchMessage message);
}
