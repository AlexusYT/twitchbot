package ru.alexus.twitchbot.twitch.commands.broadcaster;

import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.shared.ChannelOld;
import ru.alexus.twitchbot.twitch.*;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;

public class DisableCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {


		if(botChannel.endSession()){
			Globals.log.info("Session ended for channel "+botChannel);
		}else{
			Globals.log.error("Failed to end session for channel "+botChannel);
		}
		botChannel.setEnabled(false);
		result.resultMessage = botChannel.getByeMsg();
		return result;

	}

	@Override
	public String[] getAliases() {
		return new String[]{"disable", "dis", "выключить", "выкл"};
	}
}
