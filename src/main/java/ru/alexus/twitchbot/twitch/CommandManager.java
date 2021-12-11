package ru.alexus.twitchbot.twitch;

import ru.alexus.twitchbot.*;
import ru.alexus.twitchbot.twitch.commands.*;
import ru.alexus.twitchbot.twitch.commands.broadcaster.*;
import ru.alexus.twitchbot.twitch.commands.owner.*;
import ru.alexus.twitchbot.twitch.commands.regular.*;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

import static ru.alexus.twitchbot.Utils.*;

public class CommandManager {


	static LinkedHashMap<String, CommandInfo> commands = new LinkedHashMap<>();


	static {
		/*REGULAR*/
		addCommand(new HelloCmd());
		addCommand(new HelpCmd());
		addCommand(new MeasureCmd());
		CommandInfo buggycoinsCmd = addCommand(new CoinsCmd());
		addSubCommandForCommand(buggycoinsCmd, new CoinsTransferCmd());
		addSubCommandForCommand(buggycoinsCmd, new CoinsTopCmd());

		/*BROADCASTER*/
		addSubCommandForCommand(buggycoinsCmd, new CoinsGiveCmd());
		addCommand(new EnableCmd());
		addCommand(new DisableCmd());

		/*OWNER*/
		addCommand(new JoinCmd());
		addCommand(new LeaveCmd());
		addCommand(new ShutdownCmd());

	}
	static CommandInfo addCommand(ICommand command){
		try {
			CommandInfo info = new CommandInfo();
			info.aliases = command.getAliases();
			info.description = command.getDescription();
			info.executor = command;
			info.level = command.getAccessLevel();
			if (info.level == null) info.level = getAccessLevelByPackageName(command);
			for (String alias : info.aliases) {

				if(!commands.containsKey(alias))
					commands.put(alias, info);
				else{
					CommandInfo commandInfo = commands.get(alias);
					Twitch.log.warn("Alias collision detected when registering command "+info.executor.getClass().getName()
							+"! Alias '"+alias+"' already registered by "+commandInfo.executor.getClass().getName());
				}
				commands.put(alias, info);
			}
			return info;
		}catch (Exception e){
			Twitch.log.error("Failed to register command", e);
			return null;
		}
	}
	static void addSubCommandForCommand(CommandInfo parentCommand, ICommand command){
		try {
			CommandInfo info = new CommandInfo();
			info.aliases = command.getAliases();
			info.description = command.getDescription();
			info.executor = command;
			info.level = command.getAccessLevel();
			if(info.level == null) info.level = getAccessLevelByPackageName(command);
			info.parentCommand = parentCommand;
			if (parentCommand.subCommands == null) parentCommand.subCommands = new HashMap<>();
			for (String alias : info.aliases) {
				if(!parentCommand.subCommands.containsKey(alias))
					parentCommand.subCommands.put(alias, info);
				else{
					CommandInfo commandInfo = parentCommand.subCommands.get(alias);
					Twitch.log.warn("Alias collision detected when registering subcommand "+info.executor.getClass().getName()
							+"! Alias '"+alias+"' already registered by "+commandInfo.executor.getClass().getName());
				}
			}
		}catch (Exception e){
			Twitch.log.error("Failed to register command", e);
		}
	}
	static void executeCommand(String text, MsgTags tags){
		if(!text.startsWith("!")) return;
		String[] command = text.split(" ", 3);
		String alias = command[0].substring(1).toLowerCase(Locale.ROOT);

		CommandInfo cmd = getCommand(alias);
		boolean isSubCommand = false;
		if(cmd==null) return;
		if(!tags.channel.enabled&&cmd.executor != Objects.requireNonNull(getCommand("enable")).executor) return;
		if(command.length>1&&cmd.subCommands!=null) {
			String subCmdAlias = command[1].toLowerCase(Locale.ROOT);
			CommandInfo temp = getCommand(subCmdAlias, cmd.subCommands);
			if(temp!=null) {
				isSubCommand = true;
				cmd = temp;
			}
		}
		String messageToSend;
		if(cmd.level.ordinal()<=tags.getUser().getLevel().ordinal()) {
			if(isSubCommand)
				messageToSend = cmd.executor.execute(cmd, command.length > 2 ? command[2] : "", tags);
			else {
				String args = "";
				if(command.length == 2) args = command[1];
				else if(command.length == 3) args = command[1]+" "+command[2];
				messageToSend = cmd.executor.execute(cmd, args, tags);
			}
		}else{
			switch (cmd.level){
				case SUBSCRIBER: messageToSend = "{.caller}, ты не сабскрайбер. Подпишись на канал."; break;
				case MODER: messageToSend = "{.caller}, только модераторы могут использовать эту команду!"; break;
				case BROADCASTER: messageToSend = "{.caller}, вот когда станешь стримером, тогда и сможешь использовать эту команду!"; break;
				case OWNER: messageToSend = "{.caller}, ты не имеешь права мне указывать!"; break;
				default: messageToSend = "Хм... Ты, {.caller}, какое-то бесправное существо... Жаль тебя";
			}
		}
		if(messageToSend==null) return;
		Twitch.sendMsg(Utils.replaceVars(messageToSend, tags, cmd), tags.channel, cmd.level.ordinal()>EnumAccessLevel.SUBSCRIBER.ordinal());

	}
	private static EnumAccessLevel getAccessLevelByPackageName(ICommand command){
		String packageName = command.getClass().getPackageName();
		packageName = packageName.substring(packageName.lastIndexOf(("."))+1);
		switch (packageName){
			case "owner": return EnumAccessLevel.OWNER;
			case "broadcaster": return EnumAccessLevel.BROADCASTER;
			case "moder": return EnumAccessLevel.MODER;
			case "subscriber": return EnumAccessLevel.SUBSCRIBER;
			case "regular": return EnumAccessLevel.REGULAR;
		}
		throw new RuntimeException("Unknown access level for command class "+command.getClass().getName());
	}

	public static HashMap<String, CommandInfo> getCommands() {
		return commands;
	}

	public static CommandInfo getCommand(String command){
		return getCommand(command, commands);
	}
	public static CommandInfo getCommand(String command, HashMap<String, CommandInfo> commands) {
		if(!commands.containsKey(command)) {
			command = changeLang(command);
		}
		CommandInfo commandInfo = commands.get(command);
		if(commandInfo==null) return null;
		commandInfo.calledAlias = command;
		return commandInfo;
	}
}
