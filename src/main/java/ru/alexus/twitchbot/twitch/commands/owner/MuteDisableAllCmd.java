package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;

import java.util.Map;

public class MuteDisableAllCmd extends SubCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		for(Map.Entry<Integer, BotUser> entry : botChannel.getUsersById().entrySet()){
			BotUser user = entry.getValue();
			user.setMutable(false);
			entry.setValue(user);
		}
		result.resultMessage = "{.caller}, теперь НИКТО из зрителей не может быть временно отстранён другими зрителями. P.S. Модераторы по-прежнему могут";
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"denyAll"};
	}
}
