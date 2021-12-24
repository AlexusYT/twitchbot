package ru.alexus.twitchbot.bot;

public interface IBotEvents {
	/**
	 * Called when user sent whisper message to bot
	 *
	 * @param user    - who sent a message
	 * @param message - message info
	 */
	default void onWhisper(TwitchBot bot, TwitchUser user, TwitchWhisper message) {
	}

	default String onSendingWhisper(TwitchBot bot, String message) {
		return message;
	}

	/**
	 * Called when bot throws any exception. Then bot calls onBotConnectionRetryStarted
	 *
	 * @param throwable thrown exception
	 */
	default void onBotConnectionFailure(TwitchBot bot, Throwable throwable) {
	}

	/**
	 * Called when bot successfully connected to twitch
	 */
	default void onBotConnectionSuccessful(TwitchBot bot) {
	}

	/**
	 * Called when bot about to finish execution
	 */
	default void onBotStopping(TwitchBot bot) {
	}

	/**
	 * Called when bot finished execution
	 */
	default void onBotStopped(TwitchBot bot) {
	}

	/**
	 * Called when bot trying to reconnect
	 *
	 * @return true if bot should reconnect, false otherwise
	 */
	default boolean onBotConnectionRetryStarted(TwitchBot bot) {
		return true;
	}
}
