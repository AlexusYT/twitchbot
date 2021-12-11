package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

public class ShutdownCmd implements ICommand {
	@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		Twitch.shutdownBot();
		return "Я выключаюсь";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"shutdown"};
	}
}
