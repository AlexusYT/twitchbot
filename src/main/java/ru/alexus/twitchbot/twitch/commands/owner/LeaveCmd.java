package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.bot.TwitchUser;
import ru.alexus.twitchbot.bot.TwitchWhisper;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;

public class LeaveCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {


		if(args[0].isEmpty()||args[0].equalsIgnoreCase(botChannel.getName())){
			botChannel.leaveChannel(botChannel.getName());
			result.resultMessage = "Я покидаю этот чат!";
		}else{
			botChannel.leaveChannel(args[0]);
			result.resultMessage = "Я покидаю чат канала "+args[0];
		}
		return result;
	}

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchWhisper twitchWhisper, Twitch bot, TwitchUser caller, CommandResult result) {

		bot.leaveChannel(args[0]);
		result.resultMessage = "Я покидаю чат канала "+args[0];

		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"leave"};
	}
}
