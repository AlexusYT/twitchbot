package ru.alexus.twitchbot.twitch.commands;

import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.bot.AccessLevels;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.broadcaster.CoinsGiveCmd;
import ru.alexus.twitchbot.twitch.commands.broadcaster.DisableCmd;
import ru.alexus.twitchbot.twitch.commands.broadcaster.EnableCmd;
import ru.alexus.twitchbot.twitch.commands.moder.DeathCmd;
import ru.alexus.twitchbot.twitch.commands.owner.*;
import ru.alexus.twitchbot.twitch.commands.regular.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

import static ru.alexus.twitchbot.Utils.changeLang;
import static ru.alexus.twitchbot.Utils.replaceVar;

public class CommandManager {


	static LinkedHashMap<String, CommandInfo> commands = new LinkedHashMap<>();

	public static void registerAll() {
		/*REGULAR*/
		addCommand(new HelloCmd());
		addCommand(new HelpCmd());
		addCommand(new MeasureCmd());
		addCommand(new CoinsTopCmd());
		addCommand(new CasinoCmd());

		CommandInfo dictionaryCmd = addCommand(new DictionaryCmd());
		addSubCommandForCommand(dictionaryCmd, new DictionaryAddCmd());

		CommandInfo coinsCmd = addCommand(new CoinsCmd());
		addSubCommandForCommand(coinsCmd, new CoinsTransferCmd());
		addSubCommandForCommand(coinsCmd, new CoinsTopCmd());
		addSubCommandForCommand(coinsCmd, new CoinsCheckCmd());

		CommandInfo muteCmd = addCommand(new MuteCmd());
		addSubCommandForCommand(muteCmd, new MuteDisableCmd());
		addSubCommandForCommand(muteCmd, new MuteEnableCmd());
		/*MODERS*/
		addCommand(new DeathCmd());


		/*BROADCASTER*/
		addSubCommandForCommand(coinsCmd, new CoinsGiveCmd());
		addCommand(new EnableCmd());
		addCommand(new DisableCmd());

		/*OWNER*/
		addCommand(new JoinCmd());
		addCommand(new LeaveCmd());
		addCommand(new ShutdownCmd());
		addCommand(new CasinoTestCmd());
		addCommand(new ResetAllCdCmd());
		addCommand(new ReloadCmd());
		addSubCommandForCommand(muteCmd, new MuteDisableAllCmd());
		addSubCommandForCommand(muteCmd, new MuteEnableAllCmd());

	}

	public static void unregisterAll() {
		commands.clear();
	}

	static CommandInfo addCommand(ICommand command) {
		try {
			CommandInfo info = new CommandInfo();
			info.aliases = command.getAliases();
			info.description = command.getDescription();
			info.executor = command;
			info.levels = command.getAccessLevel();
			if (info.levels == 0) info.levels = getAccessLevelByPackageName(command);
			for (String alias : info.aliases) {
				alias = alias.toLowerCase(Locale.ROOT);
				if (!commands.containsKey(alias))
					commands.put(alias, info);
				else {
					CommandInfo commandInfo = commands.get(alias);
					Globals.log.warn("Alias collision detected when registering command " + info.executor.getClass().getName()
							+ "! Alias '" + alias + "' already registered by " + commandInfo.executor.getClass().getName());
				}
				commands.put(alias, info);
			}
			return info;
		} catch (Exception e) {
			Globals.log.error("Failed to register command", e);
			return null;
		}
	}

	static void addSubCommandForCommand(CommandInfo parentCommand, ICommand command) {
		try {
			CommandInfo info = new CommandInfo();
			info.aliases = command.getAliases();
			info.description = command.getDescription();
			info.executor = command;
			info.levels = command.getAccessLevel();
			if (info.levels == 0) info.levels = getAccessLevelByPackageName(command);
			info.parentCommand = parentCommand;
			if (parentCommand.subCommands == null) parentCommand.subCommands = new HashMap<>();
			for (String alias : info.aliases) {
				alias = alias.toLowerCase(Locale.ROOT);
				if (!parentCommand.subCommands.containsKey(alias))
					parentCommand.subCommands.put(alias, info);
				else {
					CommandInfo commandInfo = parentCommand.subCommands.get(alias);
					Globals.log.warn("Alias collision detected when registering subcommand " + info.executor.getClass().getName()
							+ "! Alias '" + alias + "' already registered by " + commandInfo.executor.getClass().getName());
				}
			}
		} catch (Exception e) {
			Globals.log.error("Failed to register command", e);
		}
	}

	public static String executeCommand(TwitchMessage twitchMessage, BotChannel channel, BotUser user) {
		String text = twitchMessage.getText();
		if (twitchMessage.isReplying()) {
			text = text.substring(text.indexOf(" ") + 1);
		}
		if (!text.startsWith("!")) return null;
		String[] command = text.split(" ", 3);
		String alias = command[0].substring(1).toLowerCase(Locale.ROOT);

		CommandInfo cmd = getCommand(alias);
		if (cmd == null) return null;
		//if (!channel.isEnabled() && cmd.executor != Objects.requireNonNull(getCommand("enable")).executor) return null;
		if (command.length > 1 && cmd.subCommands != null) {
			String subCmdAlias = command[1].toLowerCase(Locale.ROOT);
			CommandInfo temp = getCommand(subCmdAlias, cmd.subCommands);
			if (temp != null) {
				cmd = temp;
			}
		}

		if (!canExecute(cmd, channel, user)) return null;
		if (cmd.levels > user.getTwitchUser().getLevels()) {
			if (cmd.levels <= AccessLevels.SUBSCRIBER)
				return "{.caller}, ты не сабскрайбер! Подпишись на канал для начала";
			if (cmd.levels <= AccessLevels.MODER) return "{.caller}, только модераторы могут использовать эту команду!";
			if (cmd.levels <= AccessLevels.BROADCASTER)
				return "{.caller}, вот когда станешь стримером, тогда и сможешь использовать эту команду!";
			if (cmd.levels <= AccessLevels.OWNER) return "{.caller}, ты не имеешь права мне указывать!";
			return "Хм... Ты, {.caller}, какое-то бесправное существо... Жаль тебя";
		}

		CommandResult result;
		if (cmd.parentCommand != null) {
			String textCmd = command.length > 2 ? command[2] : "";
			result = cmd.executor.execute(cmd, textCmd, textCmd.split(" "), twitchMessage, channel, user, new CommandResult());
		} else {
			String args = "";
			if (command.length == 2) args = command[1];
			else if (command.length == 3) args = command[1] + " " + command[2];
			result = cmd.executor.execute(cmd, args, args.split(" "), twitchMessage, channel, user, new CommandResult());
		}
		String ret = null;
		if (result != null) {
			if (result.coinCost <= 0) {
				ret = result.resultMessage;
			} else if (result.sufficientCoins) {
				Globals.log.info("Removed " + result.coinCost + " coins from " + user);
				user.removeCoins(result.coinCost);
				channel.commandsLastExecute.put(cmd, System.currentTimeMillis());
				channel.commandsExecuteCount.put(cmd, channel.commandsExecuteCount.getOrDefault(cmd, 0) + 1);
				user.commandsLastExecute.put(cmd, System.currentTimeMillis());
				user.commandsExecuteCount.put(cmd, user.commandsExecuteCount.getOrDefault(cmd, 0) + 1);

				ret = result.resultMessage;
			} else {
				ret = "{.caller}, недостаточно средств. На счету {coins}, а нужно " + result.coinCost;
			}
		}

		CommandInfo mainCommand = cmd.parentCommand != null ? cmd.parentCommand : cmd;
		CommandInfo subCommand = null;
		if (cmd.parentCommand != null && cmd.subCommands == null) {
			subCommand = cmd;
		}

		ret = replaceVar("alias", mainCommand.calledAlias, ret);
		if (subCommand != null)
			ret = replaceVar("subalias", subCommand.calledAlias, ret);

		return ret;
	}


	public static void resetAllCd(BotChannel channel) {
		channel.commandsExecuteCount.clear();
		channel.commandsLastExecute.clear();
		for (BotUser user : channel.getUsersById().values()) {
			user.commandsExecuteCount.clear();
			user.commandsLastExecute.clear();
		}

	}

	private static boolean canExecute(CommandInfo info, BotChannel channel, BotUser user) {
		final long currentTime = System.currentTimeMillis();

		long lastChannelCmdCall = channel.commandsLastExecute.getOrDefault(info, 0L);
		int channelCmdCount = channel.commandsExecuteCount.getOrDefault(info, 0);
		long lastUserCmdCall = user.commandsLastExecute.getOrDefault(info, 0L);
		int userCmdCount = user.commandsExecuteCount.getOrDefault(info, 0);
		long maxChannelCmdCalls = info.executor.getGlobalMaxCalls();
		long maxUserCmdCalls = info.executor.getUserMaxCalls(user);

		if (lastChannelCmdCall + info.executor.getGlobalCooldown() * 1000 > currentTime) return false;
		if (channelCmdCount >= maxChannelCmdCalls && maxChannelCmdCalls > 0) return false;
		if (lastUserCmdCall + info.executor.getUserCooldown(user) * 1000 > currentTime) return false;
		return userCmdCount < maxUserCmdCalls || maxUserCmdCalls <= 0;
	}

	private static int getAccessLevelByPackageName(ICommand command) {
		String packageName = command.getClass().getPackageName();
		packageName = packageName.substring(packageName.lastIndexOf((".")) + 1);
		switch (packageName) {
			case "owner":
				return AccessLevels.OWNER;
			case "broadcaster":
				return AccessLevels.BROADCASTER;
			case "moder":
				return AccessLevels.MODER;
			case "subscriber":
				return AccessLevels.SUBSCRIBER;
			case "regular":
				return AccessLevels.REGULAR;
		}
		throw new RuntimeException("Unknown access level for command class " + command.getClass().getName());
	}

	public static HashMap<String, CommandInfo> getCommands() {
		return commands;
	}

	public static CommandInfo getCommand(String command) {
		return getCommand(command, commands);
	}

	public static CommandInfo getCommand(String command, HashMap<String, CommandInfo> commands) {
		if (!commands.containsKey(command)) {
			command = changeLang(command);
		}
		CommandInfo commandInfo = commands.get(command);
		if (commandInfo == null) return null;
		commandInfo.calledAlias = command;
		return commandInfo;
	}
}
