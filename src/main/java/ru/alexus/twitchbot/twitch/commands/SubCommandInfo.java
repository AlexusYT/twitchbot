package ru.alexus.twitchbot.twitch.commands;

import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;

public abstract class SubCommandInfo implements ICommand{

	/*@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {

		String result = "{.caller}, ";
		if(alias.description==null) {
			result += "описание этой команды неизвестно";
		}else{
			result += "эта команда нужна чтобы " + alias.description;
		}

		return result;
	}*/

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		result.resultMessage = "{.caller}, ";
		if(command.description==null) {
			result.resultMessage += "описание этой команды неизвестно";
		}else{
			result.resultMessage += "эта команда нужна чтобы " + command.description;
		}
		result.sufficientCoins = true;
		return result;
	}
}
