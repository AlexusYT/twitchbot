package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.MainCommandInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class CasinoCmd extends MainCommandInfo {
	private static final double notChance = 0.30;
	private static final double zeroChance = 0.49;
	private static final double x2Chance = 0.20;

	LinkedList<String> words = new LinkedList<>(List.of("все", "всё", "оллин", "аллин", "вабанк", "вобанк", "all", "allin", "vabank", "vabanque"));
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		if(args[0].isEmpty()) return super.execute(command, text, args, twitchMessage, botChannel, caller, result);
		String word = args[0].toLowerCase(Locale.ROOT).replaceAll("-", "");
		int bet;
		if(words.contains(word)){
			bet = caller.getCoins();
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
			if(caller.getCoins()<bet) {
				result.resultMessage = "{.caller}, у тебя есть только {coins}";
				return result;
			}
		}

		int resultBet = runRandom(bet);
		caller.addCoins(resultBet);
		result.resultMessage = "У {.caller} выпало ";
		if(resultBet<0) result.resultMessage += "x0 и он потерял "+Utils.pluralizeCoin(bet);
		else if(resultBet==0) result.resultMessage += "x1 и он вышел в 0";
		else if(resultBet==bet*2) result.resultMessage += "x2 и он заработал "+Utils.pluralizeCoin(bet);
		else result.resultMessage += "x50 и он получил "+Utils.pluralizeCoin(resultBet);

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
	public long getUserCooldown(BotUser user) {
		if(user.isRegular()) return 20*60;
		return 5*60;
	}

}
