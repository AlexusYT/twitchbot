package ru.alexus.twitchbot.twitch.commands;

import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

public interface ICommand {
	String execute(CommandInfo alias, String text, MsgTags tags);

	default String getDescription() {
		return null;
	}

	default EnumAccessLevel getAccessLevel(){
		return null;
	}

	String[] getAliases();
}
