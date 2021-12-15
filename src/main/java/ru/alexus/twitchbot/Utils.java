package ru.alexus.twitchbot;

import org.apache.commons.lang3.text.WordUtils;
import ru.alexus.twitchbot.langTypos.LangTypos_v2;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.Profiler;
import ru.alexus.twitchbot.twitch.WordCases;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {
	public static LangTypos_v2 converter;
	private static HashMap<Character, Character> charsRuEn = new HashMap<>();
	private static HashMap<Character, Character> charsEnRu = new HashMap<>();
	public static void init(){
		String ruChars = "йцукенгшщзхъфывапролджэячсмитьбю.,!\"№;%:?*()_+";
		String enChars = "qwertyuiop[]asdfghjkl;'zxcvbnm,./?!@#$%^&*()_+";
		for(int i = 0; i < ruChars.length(); i++){
			charsRuEn.put(ruChars.charAt(i), enChars.charAt(i));
			charsEnRu.put(enChars.charAt(i), ruChars.charAt(i));
		}
		Profiler.start("Creating LangTypos_v2");
		converter = new LangTypos_v2(); /** создаём объект класса */
		Profiler.endAndPrint();
		Profiler.start("loadDictionaries");
		converter.loadDictionaries(); /** загружаем словари */
		Profiler.endAndPrint();
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
		Profiler.start("replaceVars");
		message = replaceVar("caller", tags.getUser().getDisplayName(), message);
		message = replaceVar("coins", Utils.pluralizeMessageCoin(tags.getUser().getBuggycoins()), message);
		if(alias==null){
			Profiler.endAndPrint();
			return message;
		}
		CommandInfo mainCommand = alias.parentCommand != null ? alias.parentCommand : alias;
		CommandInfo subCommand = null;
		if(alias.parentCommand!=null&&alias.subCommands==null){
			subCommand = alias;
		}

		message = replaceVar("alias", mainCommand.calledAlias, message);
		if(subCommand!=null)
			message = replaceVar("subalias", subCommand.calledAlias, message);

		Profiler.endAndPrint();
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
	public static String pluralizeMessageCoin(int value){
		return Utils.pluralizeMessage(value, "коин", "коина", "коинов");

	}

	public static WordCases getWordCase(String word){
		WordCases cases = new WordCases();
		try {
			URLConnection connection = new URL("https://sklonili.ru/"+ URLEncoder.encode(word, StandardCharsets.UTF_8)).openConnection();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String str;
			while ((str = reader.readLine())!=null){
				if(str.contains("в множественном числе")) break;
				if(!(str.contains("тельный")||str.contains("Предложный"))) continue;
				String caseStr = str.substring(str.indexOf("Склонение'>")+11);
				caseStr = caseStr.substring(0, caseStr.indexOf("</td>"));
				if(str.contains("Именительный")) cases.nominative = caseStr;
				else if(str.contains("Родительный")) cases.genitive = caseStr;
				else if(str.contains("Дательный")) cases.dative = caseStr;
				else if(str.contains("Винительный")) cases.accusative = caseStr;
				else if(str.contains("Творительный")) cases.instrumental = caseStr;
				else if(str.contains("Предложный")) cases.prepositional = caseStr;

			}

		}catch (Exception e){
			return null;
		}
		return cases;
	}
	public static String getHash(byte[] bytes, String hash) {
		try {
			MessageDigest instance = MessageDigest.getInstance(hash);
			instance.reset();
			instance.update(bytes);
			StringBuilder sb = new StringBuilder();
			for(byte b : instance.digest()) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}
	public static String sendPost(String address, HashMap<String, String> headers, String data) throws IOException {
		HttpURLConnection http = (HttpURLConnection) new URL(address).openConnection();
		http.setRequestMethod("POST");

		http.setDoOutput(true);
		if(headers!=null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				http.setRequestProperty(header.getKey(), header.getValue());
			}
		}
		final DataOutputStream out = new DataOutputStream(http.getOutputStream());
		out.writeBytes(data);
		out.close();

		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				builder.append(line).append("\n");
			}
		}catch (Exception e){

			BufferedReader br = new BufferedReader(new InputStreamReader(http.getErrorStream()));
			String line;
			while ((line = br.readLine()) != null) {
				builder.append(line).append("\n");
			}
		}
		return builder.toString();
	}

	public static String sendGet(String address, HashMap<String, String> headers, String data) throws IOException {
		HttpURLConnection http = (HttpURLConnection) new URL(address+"?"+data).openConnection();
		http.setRequestMethod("GET");

		if(headers!=null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				http.setRequestProperty(header.getKey(), header.getValue());
			}
		}

		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				builder.append(line).append("\n");
			}
		}catch (Exception e){

			BufferedReader br = new BufferedReader(new InputStreamReader(http.getErrorStream()));
			String line;
			while ((line = br.readLine()) != null) {
				builder.append(line).append("\n");
			}
		}
		return builder.toString();
	}
	public static int sendDelete(String address, HashMap<String, String> headers, String data) throws IOException {
		HttpURLConnection http = (HttpURLConnection) new URL(address+"?"+data).openConnection();
		http.setRequestMethod("DELETE");

		if(headers!=null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				http.setRequestProperty(header.getKey(), header.getValue());
			}
		}

		return http.getResponseCode();
	}
	public static String generateSecret() {
		StringBuilder secret = new StringBuilder();
		for (int i = 0; i < random.nextInt(60, 90); i++) {
			secret.append(Integer.toHexString(random.nextInt(0, 15)));
		}
		return secret.toString();
	}
}
