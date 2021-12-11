package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

public class JoinCmd implements ICommand {
	@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		String channelName = text.split(" ")[0];
		Twitch.joinChannel(channelName);
		return "Присоединяюсь к чату "+channelName;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"join"};
	}
}
