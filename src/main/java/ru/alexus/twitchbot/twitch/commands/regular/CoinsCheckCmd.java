package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;

public class CoinsCheckCmd extends SubCommandInfo {
	@Override
	public CommandResult execute(CommandInfo commandInfo, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {

		BotUser targetUser = botChannel.getUserByName(args[0]);
		if(args[0].isEmpty()||(targetUser!=null&&targetUser.getUserId()==caller.getUserId())) {
			result.resultMessage = "{.caller}, у тебя {coins}";
			return result;
		}
		if (targetUser == null){
			result.resultMessage = "{.caller}, человек с ником " + args[0] + " ни разу писал в чат за этот стрим";
			return result;
		}

		result = caller.checkSufficientCoins(commandInfo);
		if(!result.sufficientCoins) return result;

		result.resultMessage = "{.caller}, у пользователя "+targetUser.getDisplayName()+" "+ Utils.pluralizeCoin(targetUser.getCoins());

		return result;
	}


	@Override
	public String[] getAliases() {
		return new String[]{"посмотреть", "сколько", "показать", "check"};
	}

	@Override
	public String getDescription() {
		return "показать количество коинов у себя или у указанного пользователя";
	}

	@Override
	public int getCoinCost(BotUser user) {
		if(user.isSubscriber()||user.isOwner()||user.isBroadcaster()) return 0;
		return 100;
	}
}
