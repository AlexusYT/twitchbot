package ru.alexus.twitchbot.twitch;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Profiler {
	private static final LinkedHashMap<String, Long> timeStart = new LinkedHashMap<>();
	private static final LinkedHashMap<String, Long> timeEnd = new LinkedHashMap<>();
	private static LinkedList<String> lastOpened = new LinkedList<>();
	public static boolean enable = false;

	public static void start(String name){
		try {
			if(!enable) return;
			lastOpened.addLast(name);
			timeStart.put(name, System.currentTimeMillis());
			System.out.println(name + " start");
		}catch (Exception ignored){}
	}
	public static void end(String name){
		try {
			if(!enable) return;
			timeEnd.put(name, System.currentTimeMillis()-timeStart.get(name));
		}catch (Exception ignored){}
	}
	public static void endAndPrint(){
		endAndPrint(null);
	}
	public static void endAndPrint(String name){
		try {
			if (!enable) return;
			if(name==null) name = lastOpened.getLast();
			end(name);
			System.out.println(name + " took " + timeEnd.get(name));
			lastOpened.removeLast();
		}catch (Exception ignored){}
	}

}
