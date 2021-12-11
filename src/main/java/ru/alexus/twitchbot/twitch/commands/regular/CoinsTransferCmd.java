package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;


public class CoinsTransferCmd implements ICommand {
	@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		String[] args = text.split(" ");
		System.out.println(text);
		if(args.length!=2){
			return "{.caller}, эта команда нужна для того, чтобы "+alias.description;
		}

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
				return "{.caller}, у тебя есть только "+ Utils.pluralizeMessage(currentUser.getBuggycoins(), "коин", "коина", "коинов");
		}catch (Exception ignored){
			return "{.caller}, введи корректную сумму для перевода";
		}
		currentUser.addBuggycoins(-sum);
		targetUser.addBuggycoins((int) (sum*0.8));

		tags.channel.setUserById(currentUser.getUserId(), currentUser);
		tags.channel.setUserById(targetUser.getUserId(), targetUser);

		return "{.caller}, коины успешно переведены "+target+". Комиссия составила: "+Utils.pluralizeMessage((int) (sum*0.2), "коин", "коина", "коинов");
	}

	@Override
	public String getDescription() {
		return "передать другому зрителю указанное количество коинов. Комиссия перевода - 20%. Синтаксис: !{.alias} {subalias} <ник> <сумма>";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"transfer", "перевести", "передать"};
	}
}
