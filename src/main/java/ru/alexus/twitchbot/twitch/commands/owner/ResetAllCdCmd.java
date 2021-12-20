package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.*;

public class ResetAllCdCmd extends MainCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		CommandManager.resetAllCd(botChannel);
		result.resultMessage = "{.caller}, все кулдауны (откаты) команд сброшены. Можете снова развлекаться :D";
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"resetcd"};
	}
}
