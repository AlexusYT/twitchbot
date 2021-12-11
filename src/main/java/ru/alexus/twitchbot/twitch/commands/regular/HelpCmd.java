package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.CommandManager;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

import java.util.LinkedList;
import java.util.Map;

public class HelpCmd implements ICommand {
	@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		if(text.isEmpty()) {
			StringBuilder commands = new StringBuilder();
			String delim = "";
			LinkedList<ICommand> prepared = new LinkedList<>();

			for (Map.Entry<String, CommandInfo> commandsEntry : CommandManager.getCommands().entrySet()){
				String commandKey = commandsEntry.getKey();
				CommandInfo commandInfo = commandsEntry.getValue();
				if(!Utils.isRussian(commandKey)) continue;
				if(commandInfo.level.ordinal()> EnumAccessLevel.SUBSCRIBER.ordinal()) continue;
				if(prepared.contains(commandInfo.executor)) continue;
				prepared.add(commandInfo.executor);
				commands.append(delim);
				commands.append(commandsEntry.getKey());
				delim = ", ";
			}

			return "{.caller}, укажи команду, по которой ты хочешь получить помощь через !{.alias} <команда>. Доступные команды: " + commands;
		}


		String[] subCommands = text.split(" ");
		System.out.println(text);
		if(subCommands.length>1){
			CommandInfo mainCommand = CommandManager.getCommand(subCommands[0]);
			if(mainCommand==null || mainCommand.description==null || mainCommand.description.isEmpty() || mainCommand.subCommands == null||
					mainCommand.level.ordinal()>EnumAccessLevel.SUBSCRIBER.ordinal())
				return "{.caller}, я не могу помочь тебе с этой командой";

			CommandInfo subCommand = CommandManager.getCommand(subCommands[1], mainCommand.subCommands);
			if(subCommand==null || subCommand.description==null || subCommand.description.isEmpty() ||
					subCommand.level.ordinal()>EnumAccessLevel.SUBSCRIBER.ordinal())
				return "{.caller}, я не могу помочь тебе с этой командой";
			StringBuilder aliases = new StringBuilder();
			String delim = "";
			for (String al : subCommand.aliases){
				aliases.append(delim);
				aliases.append(al);
				delim = ", ";
			}
			return "{.caller}, эта команда нужна чтобы "+subCommand.description+". Её синонимы: "+aliases;
		}

		CommandInfo info = CommandManager.getCommand(text);
		if(info==null || info.description==null || info.description.isEmpty() || info.level.ordinal()>EnumAccessLevel.SUBSCRIBER.ordinal())
			return "{.caller}, я не могу помочь тебе с этой командой";
		StringBuilder aliases = new StringBuilder();
		String delim = "";
		for (String al : info.aliases){
			aliases.append(delim);
			aliases.append(al);
			delim = ", ";
		}
		return "{.caller}, эта команда нужна чтобы "+info.description+". Её синонимы: "+aliases;
	}

	@Override
	public String getDescription() {
		return "показать информацию по указанной команде";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"help", "помощь"};
	}
}
