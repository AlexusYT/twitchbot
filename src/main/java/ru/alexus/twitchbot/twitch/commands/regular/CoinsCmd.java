package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

import java.util.LinkedList;
import java.util.Map;

public class CoinsCmd implements ICommand {

	@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		StringBuilder commands = new StringBuilder();
		String delim = "";
		LinkedList<ICommand> prepared = new LinkedList<>();

		for (Map.Entry<String, CommandInfo> commandsEntry : alias.subCommands.entrySet()){
			String commandKey = commandsEntry.getKey();
			CommandInfo commandInfo = commandsEntry.getValue();
			if(!Utils.isRussian(commandKey)) continue;
			if(commandInfo.level.ordinal()> EnumAccessLevel.SUBSCRIBER.ordinal()) continue;
			if(prepared.contains(commandInfo.executor)) continue;
			prepared.add(commandInfo.executor);
			commands.append(delim).append(commandsEntry.getKey());
			delim = ", ";
		}

		return "{.caller}, эта команда нужна чтобы "+alias.description+". Доступные подкоманды: " + commands;
	}

	@Override
	public String getDescription() {
		return "управлять своими коинами";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"buggycoins", "buggycoin", "bugycoins", "bugycoin", "coins", "coin",  "коины", "баггикоины", "баггикоин", "багикоины", "багикоин", "коинс", "коин"};
	}
}
