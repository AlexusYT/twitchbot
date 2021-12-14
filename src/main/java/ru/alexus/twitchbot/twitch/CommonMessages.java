package ru.alexus.twitchbot.twitch;

public class CommonMessages {
	public static String userNotFound(String nick){
		return "{.caller}, человек с ником " + nick + " ни разу писал в чат за этот стрим";
	}

	public static String notEnoughCoins(){
		return "{.caller}, у тебя есть только {coins}";
	}
}
