package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;

public class MuteDisableCmd extends SubCommandInfo {

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		caller.setMutable(false);
		result.resultMessage = "{.caller}, теперь тебя не могут временно отстранить другие зрители. P.S. Модераторы по-прежнему могут";
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"запретить", "отключить", "deny", "disable"};
	}

	@Override
	public String getDescription() {
		return "запретить временно отстранять (мутить) себя другим зрителям. Не имеет силы, если мут приходит из-за нарушений";
	}
}
