package ru.alexus.twitchbot.twitch.commands;

import ru.alexus.twitchbot.twitch.CommandManager;
import ru.alexus.twitchbot.twitch.ICommand;
import ru.alexus.twitchbot.twitch.MsgTags;
import ru.alexus.twitchbot.twitch.Twitch;

public class HelpCmd implements ICommand {
	@Override
	public String execute(Twitch twitch, String alias, String text, MsgTags tags) {
		if(text.isEmpty())
			return "{.caller}, укажи команду, по которой ты хочешь получить информацию";
		CommandManager.CommandInfo info = CommandManager.getCommand(text);
		if(info==null) return "{.caller}, не могу помочь тебе с этой командой";
		StringBuilder aliases = new StringBuilder();
		String delim = "";
		for (String al : info.aliases){
			aliases.append(delim);
			aliases.append(al);
			delim = ", ";
		}
		return "{.caller}, эта команда нужна чтобы "+info.description+". Её синонимы: "+aliases;
	}
}
