package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class MuteDisableCmd extends SubCommandInfo {

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		caller.setMutableByOthers(false);
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
