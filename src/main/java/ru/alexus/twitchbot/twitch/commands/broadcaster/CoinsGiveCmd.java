package ru.alexus.twitchbot.twitch.commands.broadcaster;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class CoinsGiveCmd implements ICommand {

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

		int sum;
		try{
			sum = Integer.parseInt(args[1]);
		}catch (Exception ignored){
			return "{.caller}, введи корректную сумму для перевода";
		}
		targetUser.addBuggycoins(sum);
		System.out.println(sum);
		tags.channel.setUserById(targetUser.getUserId(), targetUser);

		return "{.caller}, коины успешно выданы "+target+". Теперь у него "+
				Utils.pluralizeMessage(targetUser.getBuggycoins(), "коин", "коина", "коинов");

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
