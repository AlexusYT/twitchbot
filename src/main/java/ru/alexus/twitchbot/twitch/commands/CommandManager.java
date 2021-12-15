package ru.alexus.twitchbot.twitch.commands;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.twitch.Twitch;
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
		addCommand(new CoinsTopCmd());
		addCommand(new CasinoCmd());

		CommandInfo dictionaryCmd = addCommand(new DictionaryCmd());
		addSubCommandForCommand(dictionaryCmd, new DictionaryAddCmd());

		CommandInfo buggycoinsCmd = addCommand(new CoinsCmd());
		addSubCommandForCommand(buggycoinsCmd, new CoinsTransferCmd());
		addSubCommandForCommand(buggycoinsCmd, new CoinsTopCmd());
		addSubCommandForCommand(buggycoinsCmd, new CoinsCheckCmd());

		CommandInfo muteCmd = addCommand(new MuteCmd());
		addSubCommandForCommand(muteCmd, new MuteDisableCmd());
		addSubCommandForCommand(muteCmd, new MuteEnableCmd());

		/*BROADCASTER*/
		addSubCommandForCommand(buggycoinsCmd, new CoinsGiveCmd());
		addCommand(new EnableCmd());
		addCommand(new DisableCmd());

		/*OWNER*/
		addCommand(new JoinCmd());
		addCommand(new LeaveCmd());
		addCommand(new ShutdownCmd());
		addCommand(new CasinoTestCmd());

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
					Globals.log.warn("Alias collision detected when registering command "+info.executor.getClass().getName()
							+"! Alias '"+alias+"' already registered by "+commandInfo.executor.getClass().getName());
				}
				commands.put(alias, info);
			}
			return info;
		}catch (Exception e){
			Globals.log.error("Failed to register command", e);
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
					Globals.log.warn("Alias collision detected when registering subcommand "+info.executor.getClass().getName()
							+"! Alias '"+alias+"' already registered by "+commandInfo.executor.getClass().getName());
				}
			}
		}catch (Exception e){
			Globals.log.error("Failed to register command", e);
		}
	}
	@Nullable
	public static CommandInfo extractCommand(String text, @NonNull MsgTags tags){

		if(tags.isReplying()){
			text = text.substring(text.indexOf(" ")+1);
		}
		if(!text.startsWith("!")) return null;
		String[] command = text.split(" ", 3);
		String alias = command[0].substring(1).toLowerCase(Locale.ROOT);

		CommandInfo cmd = getCommand(alias);
		if(cmd==null) return null;
		if(!tags.channel.enabled&&cmd.executor != Objects.requireNonNull(getCommand("enable")).executor) return null;
		if(command.length>1&&cmd.subCommands!=null) {
			String subCmdAlias = command[1].toLowerCase(Locale.ROOT);
			CommandInfo temp = getCommand(subCmdAlias, cmd.subCommands);
			if(temp!=null) {
				cmd = temp;
			}
		}
		return cmd;
	}
	public static String executeCommand(String text, MsgTags tags, @Nullable CommandInfo cmd){
		if(cmd==null || !canExecute(cmd, tags)) return null;
		if(cmd.level.ordinal()>tags.getUser().getLevel().ordinal()) {
			String warning;
			switch (cmd.level){
				case SUBSCRIBER: warning = "{.caller}, ты не сабскрайбер. Подпишись на канал."; break;
				case MODER: warning = "{.caller}, только модераторы могут использовать эту команду!"; break;
				case BROADCASTER: warning = "{.caller}, вот когда станешь стримером, тогда и сможешь использовать эту команду!"; break;
				case OWNER: warning = "{.caller}, ты не имеешь права мне указывать!"; break;
				default: warning = "Хм... Ты, {.caller}, какое-то бесправное существо... Жаль тебя";
			}
			return warning;
		}
		String[] command = text.split(" ", 3);

		CommandResult result;
		if(cmd.parentCommand!=null) {
			String textCmd =  command.length > 2 ? command[2] : "";
			result = cmd.executor.execute(cmd, textCmd, textCmd.split(" "), tags, tags.channel, tags.getUser(), new CommandResult());
		}else {
			String args = "";
			if(command.length == 2) args = command[1];
			else if(command.length == 3) args = command[1]+" "+command[2];
			result = cmd.executor.execute(cmd, args, args.split(" "), tags, tags.channel, tags.getUser(), new CommandResult());
		}

		if(result!=null) {
			if (result.sufficientCoins||result.coinCost==0) {
				tags.channel.removeCoins(tags.getUser(), result.coinCost);
				cmd.lastExecutionTimeChannelWide.put(tags.getChannelName(), System.currentTimeMillis());
				cmd.totalExecutionsChannelWide.put(tags.getChannelName(), cmd.totalExecutionsChannelWide.getOrDefault(tags.getChannelName(), 0L)+1);

				HashMap<Integer, Long> timeUser = cmd.lastExecutionTimeUserWide.getOrDefault(tags.getChannelName(), new HashMap<>());
				timeUser.put(tags.getUser().getUserId(), System.currentTimeMillis());
				cmd.lastExecutionTimeUserWide.put(tags.getChannelName(), timeUser);

				HashMap<Integer, Long> execUser = cmd.totalExecutionsUserWide.getOrDefault(tags.getChannelName(), new HashMap<>());
				execUser.put(tags.getUser().getUserId(), execUser.getOrDefault(tags.getUser().getUserId(), 0L)+1);
				cmd.totalExecutionsUserWide.put(tags.getChannelName(), execUser);
				return result.resultMessage;
			} else {
				return "{.caller}, недостаточно средств. На счету {coins}, а нужно " + result.coinCost;
			}
		}
		return null;
		//Twitch.sendMsg(Utils.replaceVars(messageToSend, tags, cmd), tags.channel, cmd.level.ordinal()>EnumAccessLevel.SUBSCRIBER.ordinal());

	}
	private static boolean canExecute(CommandInfo info, MsgTags tags){
		try {
			long current = System.currentTimeMillis();
			long globalCd = info.executor.getGlobalCooldown();
			long userCd = info.executor.getUserCooldown();
			long globalMax = info.executor.getGlobalMaxCalls();
			long userMax = info.executor.getUserMaxCalls();

			String channel = tags.getChannelName();
			int userId = tags.getUser().getUserId();

			long lastExecChannel = info.lastExecutionTimeChannelWide.get(channel);
			long lastExecUser = info.lastExecutionTimeUserWide.get(channel).get(userId);
			long callsChannel = info.totalExecutionsChannelWide.get(channel);
			long callsUser = info.totalExecutionsUserWide.get(channel).get(userId);
			System.out.println(info.calledAlias+" globalCd: "+globalCd+" userCd: "+userCd+" globalMax: "+globalMax+" userMax: "+userMax);
			System.out.println(info.calledAlias+" lastExecChannel: "+lastExecChannel+" lastExecUser: "+lastExecUser
					+" callsChannel: "+callsChannel+" callsUser: "+callsUser);
			return ((lastExecChannel + globalCd * 1000 < current||globalCd==0) &&
					(lastExecUser + userCd * 1000 < current||userCd==0) &&
					(callsChannel < globalMax||globalMax==0) &&
					(callsUser < userMax||userMax==0));
		}catch (Exception e){
			return true;
		}
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
