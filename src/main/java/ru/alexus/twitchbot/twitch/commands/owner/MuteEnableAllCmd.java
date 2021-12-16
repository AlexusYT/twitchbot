package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

import java.util.Map;

public class MuteEnableAllCmd extends SubCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		for(Map.Entry<Integer, User> entry : channel.getUsersById().entrySet()){
			User user = entry.getValue();
			user.setMutableByOthers(true);
			entry.setValue(user);
		}
		result.resultMessage = "{.caller}, теперь у ВСЕХ зрителей нет защиты перед временным отстранением другими зрителями. Да начнется анархия!";
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"allowAll"};
	}
}
