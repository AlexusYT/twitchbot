package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.WordCases;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.MainCommandInfo;

public class MeasureCmd extends MainCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		if (args[0].isEmpty()) return super.execute(command, text, args, twitchMessage, botChannel, caller, result);

		int val = Utils.random.nextInt(40) - 2;
		int ed = Utils.random.nextInt(3);
		String s = "";
		if (ed == 0) s = " мм";
		if (ed == 1) s = " cм";
		if (ed == 2) s = " км";
		String word = args[0];
		if (Utils.isRussian(word)) {
			WordCases cases = Utils.getWordCase(word);
			if (cases != null && cases.genitive != null && !cases.genitive.isEmpty()) word = cases.genitive;
		}
		switch (word) {
			case "меня", "роста" -> result.resultMessage = "{.caller}, твой рост равен " + val + s;
			case "его" -> result.resultMessage = "{.caller}, его рост равен " + val + s;
			case "её" -> result.resultMessage = "{.caller}, её рост равен " + val + s;
			case "их" -> result.resultMessage = "{.caller}, их рост равен " + val + s;
			default -> result.resultMessage = "{.caller}, длина " + word + " равна " + val + s;
		}

		return result;

	}
	//https://sklonili.ru/алиас
	/*@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		if(text.isEmpty()) return super.execute(alias, text, tags);
		int val = Utils.random.nextInt(40)-2;
		int ed = Utils.random.nextInt(3);
		String s = "";
		if(ed==0) s=" мм";
		if(ed==1) s=" cм";
		if(ed==2) s=" км";

		String word = text.split(" ")[0];
		word = word.replaceAll("ё", "е");
		switch (word) {
			case "я":
			case "меня":
			case "мне":
			case "мной":
			case "мною":
			case "мой":
			case "себя":
			case "себе":
			case "собой":
			case "собою":
				return "Рост {CALLER} равен " + val + s;
			case "ты":
			case "тебя":
			case "твой":
			case "тебе":
			case "тобой":
			case "тобою":
				return "{CALLER}, твой рост равен " + val + s;
			case "он":
			case "оно":
			case "его":
			case "ему":
				return "{CALLER}, его рост равен " + val + s;
			case "она":
			case "ей":
			case "ее":
			case "ею":
				return "{CALLER}, её рост равен " + val + s;
			case "мы":
			case "нас":
			case "нам":
			case "нами":
			case "вы":
			case "вас":
			case "вам":
			case "вами":
				return "{CALLER}, ваш рост равен " + val + s;
			case "они":
			case "их":
			case "им":
			case "ими":
				return "{CALLER}, их рост равен " + val + s;
		}

		char lastChar = word.charAt(word.length() - 1);
		String tmp = word.substring(0, word.length() - 1);
		if(is_vowel(lastChar)){
			if (lastChar == 'а')
				if (is_special(word.charAt(word.length() - 2))) {
					word = tmp + "и";
				} else {
					word = tmp + "ы";
				}
			else {
				word = tmp + "и";
			}
		}else if(lastChar=='ь'){

			word = tmp + "и";
			//word = word+"а";
		}

		return "{.caller}, длина "+word + " равна "+ val + s;
	}*/

	@Override
	public String getDescription() {
		return "измерить длину какого-либо существительного. Склонение работает не всегда корректно";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"measure", "length", "измерить", "длина"};
	}

	private static boolean is_special(char ch) {
		return ch == 'г' || ch == 'ж' || ch == 'к' || ch == 'х' || ch == 'ч' || ch == 'ш' || ch == 'щ';
	}

	private static boolean is_vowel(char ch) {
		return ch == 'а' || ch == 'е' || ch == 'ё' || ch == 'и' || ch == 'о' ||
				ch == 'у' || ch == 'ы' || ch == 'э' || ch == 'ю' || ch == 'я' ||
				ch == 'А' || ch == 'Е' || ch == 'Ё' || ch == 'И' || ch == 'О' ||
				ch == 'У' || ch == 'Ы' || ch == 'Э' || ch == 'Ю' || ch == 'Я';
	}
}
