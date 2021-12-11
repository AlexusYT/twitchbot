package ru.alexus.twitchbot;

import org.apache.commons.lang3.text.WordUtils;
import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

public class Utils {
	private static HashMap<Character, Character> charsRuEn = new HashMap<>();
	private static HashMap<Character, Character> charsEnRu = new HashMap<>();
	static {
		String ruChars = "йцукенгшщзхъфывапролджэячсмитьбю.";
		String enChars = "qwertyuiop[]asdfghjkl;'zxcvbnm,./";
		for(int i = 0; i < ruChars.length(); i++){
			charsRuEn.put(ruChars.charAt(i), enChars.charAt(i));
			charsEnRu.put(enChars.charAt(i), ruChars.charAt(i));
		}
	}
	public static Random random = new Random();
	private static final Pattern pattern = Pattern.compile(
			"[а-яА-ЯёЁ" +    //буквы русского алфавита
					"\\d" +         //цифры
					"\\s" +         //знаки-разделители (пробел, табуляция и т.д.)
					"\\p{Punct}" +  //знаки пунктуации
					"]*");
	public static boolean isRussian(String str){
		return pattern.matcher(str).matches();
	}
	public static String changeLang(String str){
		StringBuilder result = new StringBuilder();
		if(isRussian(str)){
			for (int i = 0; i < str.length(); i++) result.append(charsRuEn.get(str.charAt(i)));
		}else{
			for (int i = 0; i < str.length(); i++) result.append(charsEnRu.get(str.charAt(i)));
		}
		return result.toString();
	}
	public static String getRandomText(List<String> strs){
		return strs.get(random.nextInt(strs.size()));
	}
	/*
	{.varname} - value of varname will be replaced as is
	{varname} - value of varname will be replaced as lower
	{VARNAME} - value of varname will be replaced as upper
	{Varname} - value of varname will be replaced as normal (first is capital)
	{caller} - caller's nick
	{alias} - called command alias
	 */
	public static String replaceVars(String message, MsgTags tags, CommandInfo alias){
		message = replaceVar("caller", tags.getUser().getDisplayName(), message);
		if(alias==null) return message;
		CommandInfo mainCommand = alias.parentCommand != null ? alias.parentCommand : alias;
		CommandInfo subCommand = null;
		if(alias.parentCommand!=null&&alias.subCommands==null){
			subCommand = alias;
		}

		message = replaceVar("alias", mainCommand.calledAlias, message);
		if(subCommand!=null)
			message = replaceVar("subalias", subCommand.calledAlias, message);

		return message;
	}

	public static String replaceVar(String var, String value, String message){

		message = message.replaceAll("\\{\\."+var+"}", value);
		message = message.replaceAll("\\{"+var.toLowerCase(Locale.ROOT)+"}", value.toLowerCase(Locale.ROOT));
		message = message.replaceAll("\\{"+var.toUpperCase(Locale.ROOT)+"}", value.toUpperCase(Locale.ROOT));
		message = message.replaceAll("\\{"+ WordUtils.capitalizeFully(var)+"}", WordUtils.capitalizeFully(value));

		return message;
	}
	public static String pluralizeMessage(int value, String one, String many, String other){
		if(value % 10 == 1 && value != 11)
			return value+" "+one;
		else if(value % 10 > 1 && value % 10 < 5 && !(value >=12 && value<= 20))
			return value+" "+many;
		else return value+" "+other;

	}



}
