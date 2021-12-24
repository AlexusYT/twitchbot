package ru.alexus.twitchbot.twitch.commands;

import java.util.HashMap;

public class CommandInfo {
	public ICommand executor;
	public HashMap<String, CommandInfo> subCommands = null;
	public CommandInfo parentCommand;
	public String description;
	public String[] aliases;
	public String calledAlias;
	public int levels;


}
