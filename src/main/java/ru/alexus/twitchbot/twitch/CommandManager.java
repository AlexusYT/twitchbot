package ru.alexus.twitchbot.twitch;

import ru.alexus.twitchbot.*;
import ru.alexus.twitchbot.twitch.commands.*;

import java.util.HashMap;
import java.util.Locale;

import static ru.alexus.twitchbot.Utils.*;

public class CommandManager {
	public static class CommandInfo{
		public ICommand executor;
		public String description;
		public String[] aliases;
	}


	static HashMap<String, CommandInfo> commands = new HashMap<>();


	static {
		addCommand(new String[]{"привет", "ку", "hello", "hi", "privet"}, "подоровться с ботом", new HelloCmd());
		addCommand(new String[]{"помощь", "help"}, "показать информацию по указанной команде", new HelpCmd());
		addCommand(new String[]{"измерить", "длина", "measure", "length"}, "измерить длину какого-либо существительного", new MeasureCmd());
	}
	static void addCommand(String[] aliases, String desc, ICommand execute){
		CommandInfo info = new CommandInfo();
		info.aliases = aliases;
		info.description = desc;
		info.executor = execute;
		for (String alias : aliases) {
			commands.put(alias, info);
		}
	}
	static void executeCommand(String text, MsgTags tags, String channel, Twitch twitch){
		if(!text.startsWith("!")) return;
		String[] command = text.split(" ", 2);
		String alias = command[0].substring(1).toLowerCase(Locale.ROOT);

		try {
			String messageToSend = getCommand(alias).executor.execute(twitch, alias, command.length > 1 ? command[1] : "", tags);

			twitch.sendMsg(Utils.replaceVars(messageToSend, tags, channel, alias), channel);

		}catch (Exception ignored){}
	}

	public static HashMap<String, CommandInfo> getCommands() {
		return commands;
	}

	public static CommandInfo getCommand(String command) {
		if(!commands.containsKey(command)) {
			command = changeLang(command);
		}
		return commands.get(command);
	}
}
