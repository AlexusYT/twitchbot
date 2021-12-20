package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandManager;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.MainCommandInfo;

import java.util.concurrent.TimeUnit;

public class ReloadCmd extends MainCommandInfo {

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {

		CommandManager.unregisterAll();
		CommandManager.registerAll();
		Utils.deinit();
		Utils.init();
		while (!Utils.converter.isLoaded()){
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		result.resultMessage = "{.caller}, команды и ресурсы были перезагружены";
		return result;

	}

	@Override
	public String[] getAliases() {
		return new String[]{"reload"};
	}
}
