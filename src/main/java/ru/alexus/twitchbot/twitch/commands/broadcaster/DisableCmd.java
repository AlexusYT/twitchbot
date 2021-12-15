package ru.alexus.twitchbot.twitch.commands.broadcaster;

import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.*;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class DisableCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {

		Channel info = Channels.getChannel(tags.getChannelName());

		if(info.endSession()){
			Globals.log.info("Session ended for channel "+info.channelName);
		}else{
			Globals.log.error("Failed to end session for channel "+info.channelName);
		}
		info.enabled = false;
		result.resultMessage = info.goodbyeMsg;
		return result;

	}

	@Override
	public String[] getAliases() {
		return new String[]{"disable", "dis", "выключить", "выкл"};
	}
}
