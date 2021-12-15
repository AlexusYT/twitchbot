package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class MuteEnableCmd extends SubCommandInfo {

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		caller.setMutableByOthers(true);
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
