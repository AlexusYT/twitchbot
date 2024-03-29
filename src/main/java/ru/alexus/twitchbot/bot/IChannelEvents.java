package ru.alexus.twitchbot.bot;


public interface IChannelEvents {

	default void onBotChannelJoin(TwitchBot bot, TwitchChannel twitchChannel) {
	}

	default void onBotChannelLeave(TwitchBot bot, TwitchChannel twitchChannel) {
	}

	default void onBotChannelJoinFailed(TwitchBot bot, TwitchChannel twitchChannel, String reason) {
	}

	default void onUserJoin(TwitchBot bot, TwitchChannel twitchChannel, String user) {
	}

	default void onUserLeft(TwitchBot bot, TwitchChannel twitchChannel, String user) {
	}

	default void onMessage(TwitchBot bot, TwitchChannel twitchChannel, TwitchUser user, TwitchMessage message) {
	}

	default void onNotice(TwitchBot bot, TwitchChannel twitchChannel, String msgId, String msgText) {
	}

	default String onSendingMessage(TwitchBot bot, TwitchChannel twitchChannel, String message) {
		return message;
	}
}
