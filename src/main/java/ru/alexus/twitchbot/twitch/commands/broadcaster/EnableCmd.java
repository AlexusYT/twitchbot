package ru.alexus.twitchbot.twitch.commands.broadcaster;

import ru.alexus.twitchbot.twitch.*;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

public class EnableCmd implements ICommand {
	@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		Channel info = Channels.getChannel(tags.getChannelName());
		if(info.startSession()){
			Twitch.log.info("Session started for channel "+info.channelName+" with id "+info.sessionId);
		}else{
			Twitch.log.error("Failed to start session for channel "+info.channelName);
		}
		info.enabled = true;
		return info.greetingMsg;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public EnumAccessLevel getAccessLevel() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"enable", "en", "включить", "вкл"};
	}
}
