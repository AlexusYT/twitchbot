package ru.alexus.twitchbot.twitch.commands.broadcaster;

import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.bot.TwitchUser;
import ru.alexus.twitchbot.bot.TwitchWhisper;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;

public class EnableCmd implements ICommand {

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		BotChannel channel;
		if (args[0].isEmpty())
			channel = botChannel;
		else if (caller.isOwner())
			channel = botChannel.getChannelByName(args[0]);
		else {
			result.resultMessage = "Управлять сессиями на других каналах может только OWNER";
			return result;
		}
		return execute(channel, result, args[0].isEmpty());
	}

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchWhisper twitchWhisper, Twitch bot, TwitchUser caller, CommandResult result) {
		BotChannel channel;
		if (args[0].isEmpty()) {
			result.resultMessage = "Укажи название канала";
			return result;
		} else if (caller.isOwner())
			channel = bot.getChannelByName(args[0]);
		else {
			result.resultMessage = "Управлять сессиями на других каналах может только OWNER";
			return result;
		}
		return execute(channel, result, args[0].isEmpty());
	}

	private CommandResult execute(BotChannel channel, CommandResult result, boolean thisChannel) {
		if (channel == null) {
			result.resultMessage = "Сначала подключи меня к этому каналу :D";
			return result;
		}
		if (channel.isEnabled()) {
			if (thisChannel) result.resultMessage = "На этом канале сессия уже запущена";
			else result.resultMessage = "Сессия уже была запущена ранее на канале " + channel.getName();
			return result;
		}

		if(!channel.getDatabase().isConnected()){
			result.resultMessage = "Ошибка при подключении к базе данных! Часть команд не работает";
			return result;
		}
		if (channel.startSession()) {
			channel.setEnabled(true);
			if (thisChannel) result.resultMessage = channel.getGreetMsg();
			else result.resultMessage = "Сессия запущена на канале " + channel.getName();
			Globals.log.info("Session started for channel " + channel.getName() + " with id " + channel.getSessionId());
		} else {
			channel.setEnabled(false);
			if (thisChannel) result.resultMessage = "Не удалось запустить сессию";
			else result.resultMessage = "Не удалось запустить сессию на канале " + channel.getName();
			Globals.log.error("Failed to start session for channel " + channel.getName());
		}

		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"enable", "en", "включить", "вкл"};
	}
}
