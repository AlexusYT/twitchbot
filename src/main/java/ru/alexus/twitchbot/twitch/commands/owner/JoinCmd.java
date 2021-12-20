package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.bot.TwitchBot;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.bot.TwitchUser;
import ru.alexus.twitchbot.bot.TwitchWhisper;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;

public class JoinCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		if(args[0].isEmpty()){
			return result;
		}
		String channelName = args[0];
		if(botChannel.joinChannel(channelName)){
			result.resultMessage = "Присоединяюсь к чату "+channelName;
		}else{
			result.resultMessage = "Я уже подключен к чату "+channelName;
		}
		return result;
	}

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchWhisper twitchWhisper, Twitch bot, TwitchUser caller, CommandResult result) {
		if(args[0].isEmpty()){
			return result;
		}
		String channelName = args[0];
		if(bot.joinChannel(channelName)){
			result.resultMessage = "Присоединяюсь к чату "+channelName;
		}else{
			result.resultMessage = "Я уже подключен к чату "+channelName;
		}
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"join"};
	}
}
