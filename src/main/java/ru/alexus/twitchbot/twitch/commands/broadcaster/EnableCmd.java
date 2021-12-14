package ru.alexus.twitchbot.twitch.commands.broadcaster;

import ru.alexus.twitchbot.twitch.*;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class EnableCmd implements ICommand {
	/*@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		Channel info;

		if(text.isEmpty())
			info = Channels.getChannel(tags.getChannelName());
		else
			info = Channels.getChannel(text.split(" ")[0]);
		if(info.startSession()){
			Twitch.log.info("Session started for channel "+info.channelName+" with id "+info.sessionId);
		}else{
			Twitch.log.error("Failed to start session for channel "+info.channelName);
		}
		info.enabled = true;
		if(text.isEmpty()) return info.greetingMsg;
		else return "Сессия запущена на канале "+info.channelName;
	}*/

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		Channel info;
		if(args[0].isEmpty())
			info = Channels.getChannel(tags.getChannelName());
		else if(caller.getLevel()==EnumAccessLevel.OWNER)
			info = Channels.getChannel(args[0]);
		else{
			result.resultMessage = "Управлять сессиями на других каналах может только OWNER";
			return result;
		}
		if(info.startSession()){
			Twitch.log.info("Session started for channel "+info.channelName+" with id "+info.sessionId);
		}else{
			Twitch.log.error("Failed to start session for channel "+info.channelName);
		}
		info.enabled = true;

		if(args[0].isEmpty()) result.resultMessage = info.greetingMsg;
		else result.resultMessage = "Сессия запущена на канале "+info.channelName;
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"enable", "en", "включить", "вкл"};
	}
}
