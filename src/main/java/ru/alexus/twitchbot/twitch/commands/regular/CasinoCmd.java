package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.MainCommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class CasinoCmd extends MainCommandInfo {
	private static final double notChance = 0.30;
	private static final double zeroChance = 0.49;
	private static final double x2Chance = 0.20;

	LinkedList<String> words = new LinkedList<>(List.of("все", "всё", "оллин", "аллин", "вабанк", "вобанк", "all", "allin", "vabank", "vabanque"));
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		if(args[0].isEmpty()) return super.execute(command, text, args, tags, channel, caller, result);
		String word = args[0].toLowerCase(Locale.ROOT).replaceAll("-", "");
		int bet;
		if(words.contains(word)){
			bet = caller.getBuggycoins();
			if(bet<10){
				result.resultMessage = "{.caller}, у тебя должно быть не менее 10 коинов на счету, чтобы играть";
				return result;
			}
		}else{
			try{
				bet = Integer.parseInt(args[0]);
			}catch (Exception ignored){
				result.resultMessage = "{.caller}, введи корректную сумму ставки";
				return result;
			}
			if(bet<10){
				result.resultMessage = "{.caller}, ставка должна быть больше или равна 10 коинам";
				return result;
			}
			if(caller.getBuggycoins()<bet) {
				result.resultMessage = "{.caller}, у тебя есть только {coins}";
				return result;
			}
		}

		int resultBet = runRandom(bet);
		channel.addCoins(caller, resultBet);
		if(resultBet<0) result.resultMessage = "{.caller} потерял "+Utils.pluralizeMessageCoin(bet);
		else if(resultBet==0) result.resultMessage = "{.caller} вышел в 0";
		else if(resultBet==bet*2) result.resultMessage = "{.caller} получил "+Utils.pluralizeMessageCoin(resultBet);
		else result.resultMessage = "{.caller} получил "+Utils.pluralizeMessageCoin(resultBet);

		return result;
	}

	public static int runRandom(int coins){
		double value = Utils.random.nextDouble();
		if(value<notChance) return -coins;
		else if(value<zeroChance+notChance) return 0;
		else if(value<zeroChance+notChance+x2Chance) return coins*2;
		else return coins*50;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"казино", "азино", "casino", "asino"};
	}

	@Override
	public String getDescription() {
		return "попытать удачу в казино. !{alias} <ставка>";
	}

	@Override
	public long getUserCooldown(EnumAccessLevel level) {
		if(level==EnumAccessLevel.REGULAR) return 20*60;
		return 5*60;
	}

}
