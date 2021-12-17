package ru.alexus.twitchbot.bot;

public interface IBotEvents {
	/**
	 * Called when user sent whisper message to bot
	 * @param user - who sent a message
	 * @param message - message info
	 */
	void onWhisper(TwitchUser user, TwitchWhisper message);

	/**
	 * Called when bot throws any exception. Then bot calls onBotConnectionRetryStarted
	 * @param throwable thrown exception
	 */
	void onBotConnectionFailure(Throwable throwable);

	/**
	 * Called when bot successfully connected to twitch
	 */
	void onBotConnectionSuccessful();

	/**
	 * Called when bot finished execution
	 */
	void onBotStopped();

	/**
	 * Called when bot trying to reconnect
	 * @return true if bot should reconnect, false otherwise
	 */
	boolean onBotConnectionRetryStarted();
}
