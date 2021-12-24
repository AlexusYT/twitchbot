package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.CommonMessages;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;


public class CoinsTransferCmd extends SubCommandInfo {

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		if(args.length!=2) return super.execute(command, text, args, twitchMessage, botChannel, caller, result);

		BotUser targetUser = botChannel.getUserByName(args[0]);
		if(targetUser!=null&&targetUser.getUserId()==caller.getUserId()) {
			result.resultMessage = "{.caller}, нельзя перевести коины самому себе";
			return result;
		}
		if (targetUser == null){
			result.resultMessage = CommonMessages.userNotFound(args[0]);
			return result;
		}

		int sum;
		try{
			sum = Integer.parseInt(args[1]);
			if(sum<10){
				result.resultMessage = "{.caller}, сумма должна быть больше или равна 10 коинам";
				return result;
			}
			if(caller.getCoins()<sum) {
				result.resultMessage = CommonMessages.notEnoughCoins();
				return result;
			}
		}catch (Exception ignored){
			result.resultMessage = "{.caller}, введи корректную сумму для перевода";
			return result;
		}
		caller.removeCoins(sum);
		double com = 0.2;
		if(caller.isSubscriber()) com = 0.1;
		if(caller.isOwner()||caller.isBroadcaster()) com = 0.0;

		targetUser.addCoins((int) (sum*(1-com)));
		result.resultMessage = "{.caller}, коины успешно переведены "+targetUser.getDisplayName()+". Комиссия составила: "+Utils.pluralizeCoin((int) (sum*com));
		return result;

	}

	/*@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		String[] args = text.split(" ");

		if(args.length!=2) return super.execute(alias, text, tags);


		String target = args[0];
		if(target.startsWith("@")) target = target.substring(1);
		User targetUser = tags.channel.getUserByName(target);
		if(targetUser==null){
			return "{.caller}, человек с ником "+target+" ни разу писал в чат за этот стрим";
		}

		User currentUser = tags.getUser();
		if(currentUser.getUserId()==targetUser.getUserId())
			return "{.caller}, нельзя перевести коины самому себе";
		int sum;
		try{
			sum = Integer.parseInt(args[1]);
			if(sum<10) return "{.caller}, сумма должна быть больше или равна 10 коинам";
			if(currentUser.getBuggycoins()<sum)
				return "{.caller}, у тебя есть только "+ Utils.pluralizeMessageCoin(currentUser.getBuggycoins());
		}catch (Exception ignored){
			return "{.caller}, введи корректную сумму для перевода";
		}
		currentUser.addBuggycoins(-sum);
		targetUser.addBuggycoins((int) (sum*0.8));

		tags.channel.setUserById(currentUser.getUserId(), currentUser);
		tags.channel.setUserById(targetUser.getUserId(), targetUser);

		return "{.caller}, коины успешно переведены "+target+". Комиссия составила: "+Utils.pluralizeMessageCoin((int) (sum*0.2));
	}*/

	@Override
	public String getDescription() {
		return "передать другому зрителю указанное количество коинов. Комиссия перевода - 20%. Синтаксис: !{.alias} {subalias} <ник> <сумма>";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"transfer", "перевести", "передать"};
	}
}
