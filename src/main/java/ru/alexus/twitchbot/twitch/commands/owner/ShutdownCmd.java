package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class ShutdownCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {

		Twitch.shutdownBot();
		result.resultMessage = "Я выключаюсь";
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"shutdown"};
	}
}
