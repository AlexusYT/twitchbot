package ru.alexus.twitchbot.twitch.commands.broadcaster;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.CommonMessages;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;

public class CoinsGiveCmd extends SubCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		if(args.length!=2) return super.execute(command, text, args, twitchMessage, botChannel, caller, result);

		BotUser targetUser = botChannel.getUserByName(args[0]);
		if (targetUser == null){
			result.resultMessage = CommonMessages.userNotFound(args[0]);
			return result;
		}

		int sum;
		try{
			sum = Integer.parseInt(args[1]);
		}catch (Exception ignored){
			result.resultMessage = "{.caller}, введи корректное количество коинов для выдачи";
			return result;
		}
		targetUser.addCoins(sum);
		result.resultMessage = "{.caller}, коины успешно выданы "+targetUser.getDisplayName()+". Теперь у него "+ Utils.pluralizeCoin(targetUser.getCoins());
		return result;
	}

	@Override
	public String getDescription() {
		return "выдать зрителю указанное количество коинов. Синтаксис: !{.alias} {subalias} <ник> <сумма>";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"give", "дать", "выдать"};
	}
}
