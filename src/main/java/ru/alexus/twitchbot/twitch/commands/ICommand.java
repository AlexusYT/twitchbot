package ru.alexus.twitchbot.twitch.commands;

import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.bot.TwitchUser;
import ru.alexus.twitchbot.bot.TwitchWhisper;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.Twitch;

public interface ICommand {

	CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result);

	default CommandResult execute(CommandInfo command, String text, String[] args, TwitchWhisper twitchWhisper, Twitch bot, TwitchUser caller, CommandResult result){
		return result;
	}

	String[] getAliases();

	default String getDescription() {
		return null;
	}

	default int getAccessLevel(){
		return 0;
	}

	default int getCoinCost(BotUser user){
		return 0;
	}

	default long getGlobalCooldown(){
		return 0;
	}

	default long getUserCooldown(BotUser user){
		return 0;
	}

	default long getGlobalMaxCalls(){
		return 0;
	}

	default long getUserMaxCalls(BotUser user){
		return 0;
	}
}
