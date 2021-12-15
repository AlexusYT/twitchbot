package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class LeaveCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {


		if(args[0].isEmpty()||args[0].equalsIgnoreCase(tags.getChannelName())){
			Twitch.leftChannel(tags.getChannelName());
			result.resultMessage = "Я покидаю этот чат!";
		}else{
			Twitch.leftChannel(args[0]);
			result.resultMessage = "Я покидаю чат канала "+args[0];
		}
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"leave"};
	}
}
