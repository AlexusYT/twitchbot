package ru.alexus.twitchbot.twitch.commands;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

import java.util.LinkedList;
import java.util.Map;

public abstract class MainCommandInfo implements ICommand{
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		result.resultMessage = "{.caller}, ";
		if(command.description==null) {
			result.resultMessage += "описание этой команды неизвестно. ";
		}else{
			result.resultMessage += "эта команда нужна чтобы " + command.description+". ";
		}
		if(command.subCommands==null) {
			result.resultMessage += " Нет доступных подкоманд";
			return result;
		}
		StringBuilder commands = new StringBuilder();
		String delim = "";
		LinkedList<ICommand> prepared = new LinkedList<>();

		for (Map.Entry<String, CommandInfo> commandsEntry : command.subCommands.entrySet()){
			String commandKey = commandsEntry.getKey();
			CommandInfo commandInfo = commandsEntry.getValue();
			if(!Utils.isRussian(commandKey)) continue;
			if(commandInfo.level.ordinal()> EnumAccessLevel.SUBSCRIBER.ordinal()) continue;
			if(prepared.contains(commandInfo.executor)) continue;
			prepared.add(commandInfo.executor);
			commands.append(delim).append(commandsEntry.getKey());
			delim = ", ";
		}

		result.resultMessage += " Доступные подкоманды: " + commands;
		return result;
	}

}
