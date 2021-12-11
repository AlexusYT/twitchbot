package ru.alexus.twitchbot.twitch;

import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.ICommand;

import java.util.HashMap;
import java.util.List;

public class CommandInfo {
	public ICommand executor;
	public HashMap<String, CommandInfo> subCommands = null;
	public CommandInfo parentCommand;
	public String description;
	public String[] aliases;
	public String calledAlias;
	public EnumAccessLevel level;


}
