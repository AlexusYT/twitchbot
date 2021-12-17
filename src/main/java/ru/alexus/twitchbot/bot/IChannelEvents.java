package ru.alexus.twitchbot.bot;


public interface IChannelEvents {

	default void onBotChannelJoin(TwitchChannel channel){}
	default void onBotChannelLeave(TwitchChannel channel){}
	default void onBotChannelJoinFailed(TwitchChannel channel, String reason){}

	default void onUserJoin(TwitchChannel channel, String user){}
	default void onUserLeft(TwitchChannel channel, String user){}

	void onMessage(TwitchChannel channel, TwitchUser user, TwitchMessage message);
	default String onSendingMessage(TwitchChannel channel, String message){
		return message;
	}
}
