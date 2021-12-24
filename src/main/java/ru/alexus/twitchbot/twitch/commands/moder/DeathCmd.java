package ru.alexus.twitchbot.twitch.commands.moder;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.MainCommandInfo;

public class DeathCmd extends MainCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		if (args[0].isEmpty()) {
			botChannel.incDeathCounter();
			result.resultMessage = "{.caller}, стример еще раз умер! Всего их " + botChannel.getDeathCounter();
		} else {
			result.resultMessage = "{.caller}, стример умер  " + Utils.pluralizeMessage(botChannel.getDeathCounter(), "раз", "раза", "раз");
		}
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"death", "смерть"};
	}
}
