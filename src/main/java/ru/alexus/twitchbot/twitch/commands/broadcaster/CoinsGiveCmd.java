package ru.alexus.twitchbot.twitch.commands.broadcaster;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.CommonMessages;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class CoinsGiveCmd extends SubCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		if(args.length!=2) return super.execute(command, text, args, tags, channel, caller, result);

		User targetUser = channel.getUserByName(args[0]);
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
		channel.addCoins(targetUser, sum);
		result.resultMessage = "{.caller}, коины успешно выданы "+targetUser.getDisplayName()+". Теперь у него "+ Utils.pluralizeMessageCoin(targetUser.getBuggycoins());
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

		int sum;
		try{
			sum = Integer.parseInt(args[1]);
		}catch (Exception ignored){
			return "{.caller}, введи корректную сумму для перевода";
		}
		targetUser.addBuggycoins(sum);

		tags.channel.setUserById(targetUser.getUserId(), targetUser);

		return "{.caller}, коины успешно выданы "+target+". Теперь у него "+ Utils.pluralizeMessageCoin(targetUser.getBuggycoins());

	}*/

	@Override
	public String getDescription() {
		return "выдать зрителю указанное количество коинов. Синтаксис: !{.alias} {subalias} <ник> <сумма>";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"give", "дать", "выдать"};
	}
}
