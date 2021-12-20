package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;

public class ShutdownCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {

		botChannel.stopBot();
		result.resultMessage = null;
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"shutdown"};
	}
}
