package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.AccessLevels;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandManager;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;

import java.util.LinkedList;
import java.util.Map;

public class HelpCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		if(args[0].isEmpty()) {
			StringBuilder commands = new StringBuilder();
			String delim = "";
			LinkedList<ICommand> prepared = new LinkedList<>();

			for (Map.Entry<String, CommandInfo> commandsEntry : CommandManager.getCommands().entrySet()){
				String commandKey = commandsEntry.getKey();
				CommandInfo commandInfo = commandsEntry.getValue();
				if(!Utils.isRussian(commandKey)) continue;
				if(commandInfo.levels > AccessLevels.SUBSCRIBER) continue;
				if(prepared.contains(commandInfo.executor)) continue;
				prepared.add(commandInfo.executor);
				commands.append(delim);
				commands.append(commandsEntry.getKey());
				delim = ", ";
			}
			result.resultMessage = "{.caller}, укажи команду, по которой ты хочешь получить помощь через !{.alias} <команда>. Доступные команды: " + commands;
			return result;
		}

		if(args.length>1){
			CommandInfo mainCommand = CommandManager.getCommand(args[0]);
			if(mainCommand==null || mainCommand.description==null || mainCommand.description.isEmpty() || mainCommand.subCommands == null ||
					mainCommand.levels > AccessLevels.SUBSCRIBER) {
				result.resultMessage = "{.caller}, я не могу помочь тебе с этой командой";
				return result;
			}

			CommandInfo subCommand = CommandManager.getCommand(args[1], mainCommand.subCommands);
			if(subCommand==null || subCommand.description==null || subCommand.description.isEmpty() ||
					subCommand.levels > AccessLevels.SUBSCRIBER) {
				result.resultMessage = "{.caller}, я не могу помочь тебе с этой командой";
				return result;
			}
			StringBuilder aliases = new StringBuilder();
			String delim = "";
			for (String al : subCommand.aliases){
				aliases.append(delim);
				aliases.append(al);
				delim = ", ";
			}
			result.resultMessage = "{.caller}, эта команда нужна чтобы "+subCommand.description+". Её синонимы: "+aliases;
			return result;
		}
		CommandInfo info = CommandManager.getCommand(args[0]);
		if(info==null || info.description==null || info.description.isEmpty() || info.levels>AccessLevels.SUBSCRIBER) {
			result.resultMessage = "{.caller}, я не могу помочь тебе с этой командой";
			return result;
		}
		StringBuilder aliases = new StringBuilder();
		String delim = "";
		for (String al : info.aliases){
			aliases.append(delim);
			aliases.append(al);
			delim = ", ";
		}
		result.resultMessage = "{.caller}, эта команда нужна чтобы "+info.description+". Её синонимы: "+aliases;
		return result;
	}


	@Override
	public String getDescription() {
		return "показать информацию по указанной команде";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"помощь", "команды", "help", "commands"};
	}

}
