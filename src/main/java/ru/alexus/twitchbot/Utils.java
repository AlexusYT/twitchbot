package ru.alexus.twitchbot;

import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.alexus.twitchbot.langTypos.LangTypos_v2;
import ru.alexus.twitchbot.twitch.Profiler;
import ru.alexus.twitchbot.twitch.WordCases;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {
	public static LangTypos_v2 converter;
	private static final HashMap<Character, Character> charsRuEn = new HashMap<>();
	private static final HashMap<Character, Character> charsEnRu = new HashMap<>();

	public static void init() {
		String ruChars = "йцукенгшщзхъфывапролджэячсмитьбю.,!\"№;%:?*()_+";
		String enChars = "qwertyuiop[]asdfghjkl;'zxcvbnm,./?!@#$%^&*()_+";
		for (int i = 0; i < ruChars.length(); i++) {
			charsRuEn.put(ruChars.charAt(i), enChars.charAt(i));
			charsEnRu.put(enChars.charAt(i), ruChars.charAt(i));
		}
		Profiler.start("Creating LangTypos_v2");
		converter = new LangTypos_v2();
		Profiler.endAndPrint();
		Profiler.start("loadDictionaries");
		converter.loadDictionaries();
		Profiler.endAndPrint();
	}

	public static void deinit() {
		charsRuEn.clear();
		charsEnRu.clear();
		converter.unloadDictionaries();
		converter = new LangTypos_v2();
		converter.loadDictionaries();

	}

	public static Random random = new Random();
	private static final Pattern pattern = Pattern.compile(
			"[а-яА-ЯёЁ" +    //буквы русского алфавита
					"\\d" +         //цифры
					"\\s" +         //знаки-разделители (пробел, табуляция и т.д.)
					"\\p{Punct}" +  //знаки пунктуации
					"]*");

	public static boolean isRussian(String str) {
		return pattern.matcher(str).matches();
	}

	public static String changeLang(String str) {
		StringBuilder result = new StringBuilder();
		if (isRussian(str)) {
			for (int i = 0; i < str.length(); i++) result.append(charsRuEn.get(str.charAt(i)));
		} else {
			for (int i = 0; i < str.length(); i++) result.append(charsEnRu.get(str.charAt(i)));
		}
		return result.toString();
	}

	public static String getRandomText(List<String> strs) {
		return strs.get(random.nextInt(strs.size()));
	}


	public static String replaceVar(String var, String value, String message) {
		if (message == null || var == null || value == null) return null;
		message = message.replaceAll("\\{\\." + var + "}", value);
		message = message.replaceAll("\\{" + var.toLowerCase(Locale.ROOT) + "}", value.toLowerCase(Locale.ROOT));
		message = message.replaceAll("\\{" + var.toUpperCase(Locale.ROOT) + "}", value.toUpperCase(Locale.ROOT));
		message = message.replaceAll("\\{" + WordUtils.capitalizeFully(var) + "}", WordUtils.capitalizeFully(value));

		return message;
	}

	public static String pluralizeMessage(int value, String one, String many, String other) {
		if (value % 10 == 1 && value != 11)
			return value + " " + one;
		else if (value % 10 > 1 && value % 10 < 5 && !(value >= 12 && value <= 20))
			return value + " " + many;
		else return value + " " + other;

	}

	public static String pluralizeCoin(int value) {
		return Utils.pluralizeMessage(value, "коин", "коина", "коинов");
	}

	public static String pluralizePoints(int value) {
		return Utils.pluralizeMessage(value, "балл", "балла", "баллов");
	}

	public static WordCases getWordCase(String word) {
		WordCases cases = new WordCases();
		try {
			URLConnection connection = new URL("https://sklonili.ru/" + URLEncoder.encode(word, StandardCharsets.UTF_8)).openConnection();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String str;
			while ((str = reader.readLine()) != null) {
				if (str.contains("в множественном числе")) break;
				if (!(str.contains("тельный") || str.contains("Предложный"))) continue;
				String caseStr = str.substring(str.indexOf("Склонение'>") + 11);
				caseStr = caseStr.substring(0, caseStr.indexOf("</td>"));
				if (str.contains("Именительный")) cases.nominative = caseStr;
				else if (str.contains("Родительный")) cases.genitive = caseStr;
				else if (str.contains("Дательный")) cases.dative = caseStr;
				else if (str.contains("Винительный")) cases.accusative = caseStr;
				else if (str.contains("Творительный")) cases.instrumental = caseStr;
				else if (str.contains("Предложный")) cases.prepositional = caseStr;

			}

		} catch (Exception e) {
			return null;
		}
		return cases;
	}

	public static String hmacSha256(String value, String key) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));

			StringBuilder sb = new StringBuilder();
			for (byte b : mac.doFinal(value.getBytes())) sb.append(String.format("%02x", b));
			return sb.toString();

		} catch (Exception e) {
			return "";
		}
	}

	public static String sendPatch(String address, @NotNull HashMap<String, String> headers, @NotNull Map<String, String> query, @NotNull String body) throws IOException {
		return send("POST", address, headers, query, body);
	}

	public static String sendPost(String address, @Nullable HashMap<String, String> headers, @NotNull HashMap<String, String> body) throws IOException {
		StringBuilder queryString = new StringBuilder();
		String delim = "";
		for (var entry : body.entrySet()) {
			queryString.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
			delim = "&";
		}

		return send("POST", address, headers, null, queryString.toString());
	}

	public static String sendPost(String address, @Nullable HashMap<String, String> headers, @NotNull String body) throws IOException {
		return send("POST", address, headers, null, body);
	}

	public static int sendDelete(String address, @Nullable HashMap<String, String> headers, @Nullable Map<String, String> query) throws IOException {
		return Integer.parseInt(send("DELETE", address, headers, query, null));
	}

	public static String sendGet(String address, @Nullable HashMap<String, String> headers, @Nullable Map<String, String> query) throws IOException {
		return send("GET", address, headers, query, null);
	}

	public static String send(String method, String address, @Nullable HashMap<String, String> headers, @Nullable Map<String, String> query, @Nullable String body) throws IOException {
		StringBuilder queryString = new StringBuilder();
		if (query != null) {
			queryString.append("?");
			String delim = "";
			for (var entry : query.entrySet()) {
				queryString.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
				delim = "&";
			}
		}
		method = method.toUpperCase(Locale.ROOT);
		HttpURLConnection http = (HttpURLConnection) new URL(address + queryString).openConnection();
		http.setRequestMethod(method);

		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				http.setRequestProperty(header.getKey(), header.getValue());
			}
		}
		if (body != null) {
			http.setDoOutput(true);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(http.getOutputStream()));
			writer.write(body);
			writer.close();
		}
		if (method.equals("DELETE")) return String.valueOf(http.getResponseCode());
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(http.getInputStream()));
		} catch (Exception e) {
			br = new BufferedReader(new InputStreamReader(http.getErrorStream()));
		}
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			builder.append(line).append("\n");
		}
		return builder.toString();


	}


	public static String generateSecret() {
		StringBuilder secret = new StringBuilder();
		for (int i = 0; i < random.nextInt(60, 90); i++) {
			secret.append(Integer.toHexString(random.nextInt(0, 15)));
		}
		return secret.toString();
	}

	public static boolean isWebHost() {
		return System.getenv("PORT") != null;
	}

}
