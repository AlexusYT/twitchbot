package ru.alexus.twitchbot.twitch.commands;

import ru.alexus.twitchbot.twitch.Channel;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public interface ICommand {

	CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result);

	String[] getAliases();

	default String getDescription() {
		return null;
	}

	default EnumAccessLevel getAccessLevel(){
		return null;
	}

	default int getCoinCost(EnumAccessLevel level){
		return 0;
	}

	default long getGlobalCooldown(){
		return 0;
	}

	default long getUserCooldown(){
		return 0;
	}

	default long getGlobalMaxCalls(){
		return 0;
	}

	default long getUserMaxCalls(){
		return 0;
	}
}
