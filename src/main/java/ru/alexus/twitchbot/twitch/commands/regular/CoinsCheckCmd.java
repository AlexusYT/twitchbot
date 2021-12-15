package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class CoinsCheckCmd extends SubCommandInfo {
	@Override
	public CommandResult execute(CommandInfo commandInfo, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {

		User targetUser = channel.getUserByName(args[0]);
		if(args[0].isEmpty()||(targetUser!=null&&targetUser.getUserId()==caller.getUserId())) {
			result.resultMessage = "{.caller}, у тебя {coins}";
			return result;
		}
		if (targetUser == null){
			result.resultMessage = "{.caller}, человек с ником " + args[0] + " ни разу писал в чат за этот стрим";
			return result;
		}

		result = channel.checkSufficientCoins(caller, commandInfo);
		if(!result.sufficientCoins) return result;

		result.resultMessage = "{.caller}, у пользователя "+targetUser.getDisplayName()+" "+ Utils.pluralizeMessageCoin(targetUser.getBuggycoins());

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
	public int getCoinCost(EnumAccessLevel level) {
		if(level==EnumAccessLevel.SUBSCRIBER) return 0;

		return 100;
	}
}
