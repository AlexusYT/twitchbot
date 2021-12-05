package ru.alexus.twitchbot.twitch;

public interface ICommand {
	String execute(Twitch twitch, String alias, String text, MsgTags tags);
}
