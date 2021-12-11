package ru.alexus.twitchbot.twitch.commands.broadcaster;

import ru.alexus.twitchbot.twitch.*;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

public class DisableCmd implements ICommand {
	@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		Channel info = Channels.getChannel(tags.getChannelName());

		if(info.endSession()){
			Twitch.log.info("Session ended for channel "+info.channelName);
		}else{
			Twitch.log.error("Failed to end session for channel "+info.channelName);
		}
		info.enabled = false;
		return info.goodbyeMsg;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public EnumAccessLevel getAccessLevel() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"disable", "dis", "выключить", "выкл"};
	}
}
