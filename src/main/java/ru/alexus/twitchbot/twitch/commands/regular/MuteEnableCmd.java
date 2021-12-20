package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;

public class MuteEnableCmd extends SubCommandInfo {

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		caller.setMutable(true);
		result.resultMessage = "{.caller}, теперь у тебя нет защиты перед временным отстранением другими зрителями";
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"разрешить", "включить", "allow", "enable"};
	}

	@Override
	public String getDescription() {
		return "разрешить временно отстранять (мутить) себя другим зрителям";
	}
}
