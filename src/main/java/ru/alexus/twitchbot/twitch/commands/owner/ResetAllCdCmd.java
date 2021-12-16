package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.commands.*;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

import java.util.Map;

public class ResetAllCdCmd extends MainCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		CommandManager.resetAllCd(channel);
		result.resultMessage = "{.caller}, все кулдауны (откаты) команд сброшены. Можете развлекаться :D";
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"resetcd"};
	}
}
