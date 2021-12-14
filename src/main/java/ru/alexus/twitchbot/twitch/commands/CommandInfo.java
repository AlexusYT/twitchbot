package ru.alexus.twitchbot.twitch.commands;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class CommandInfo {
	public ICommand executor;
	public HashMap<String, CommandInfo> subCommands = null;
	public CommandInfo parentCommand;
	public String description;
	public String[] aliases;
	public String calledAlias;
	public EnumAccessLevel level;
	public LinkedHashMap<String, Long> lastExecutionTimeChannelWide = new LinkedHashMap<>();
	public LinkedHashMap<String, Long> totalExecutionsChannelWide = new LinkedHashMap<>();
	public LinkedHashMap<String, HashMap<Integer, Long>> lastExecutionTimeUserWide = new LinkedHashMap<>();
	public LinkedHashMap<String, HashMap<Integer, Long>> totalExecutionsUserWide = new LinkedHashMap<>();



}
