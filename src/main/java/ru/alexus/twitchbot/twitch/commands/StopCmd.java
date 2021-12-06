package ru.alexus.twitchbot.twitch.commands;

import ru.alexus.twitchbot.twitch.ICommand;
import ru.alexus.twitchbot.twitch.MsgTags;
import ru.alexus.twitchbot.twitch.Twitch;

public class StopCmd implements ICommand {
	@Override
	public String execute(Twitch twitch, String alias, String text, MsgTags tags) {
		if(!tags.getDisplayName().equals("Alexus_XX")) return "{.caller}, ты не имеешь права мне указывать!";
		twitch.setRunning(false);
		return "Всем удачи и всем пока! Squid1 Squid2 Squid3 Squid2 Squid4 ";
	}
}
