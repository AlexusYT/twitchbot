package ru.alexus.twitchbot.twitch;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


public class Twitch extends TwitchHelper {
	public static final String databaseUrl = "jdbc:mysql://slymcdb.cusovblh0zzb.eu-west-2.rds.amazonaws.com";
	public static final String databaseLogin = "admin";
	public static final String databasePass = "nBeXaR8bLByWwyF";



	public static void startBot(){
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.info("Shutting down");
			for (Channel channel : Channels.getChannels().values()) {
				channel.saveTotalMessagesToDB();
				channel.saveBuggycoinsToDB();
			}

		}));

		log.info("Twitch bot thread started");
		new Thread(TwitchHelper::senderThread).start();
		new Thread(TwitchHelper::botListUpdater).start();
		new Thread(TwitchHelper::connectionMonitor).start();
		while (!shutdown){
			try {
				connectToTwitch();
				run();
			}catch (Exception e){
				log.error("Bot crashed. Restarting", e);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {}
			}
		}
		log.info("Twitch bot thread ended");
	}


	public static void onPrivMsg(MsgTags tags, User user, String message){
		if(tags.channel.registerUser(user)){
			tags.channel.addUserToCurrentSession(user);
		}else{
			if(tags.channel.enabled)
				Twitch.log.info("Failed to add user "+user.getDisplayName());
		}
		if(tags.isFirstMsg())
			Twitch.sendMsg(Utils.replaceVars("Чатик, поздоровайтесь с {.caller}. Он первый раз на нашем канале!", tags, null), tags.channel);
		else if(user.messagesInSession==-1&&user.getLevel()!= EnumAccessLevel.BROADCASTER){
			CommandManager.executeCommand("!привет", tags);
		}
		log.info(user.getDisplayName()+": "+message);
		try {

			CommandManager.executeCommand(message, tags);
		} catch (Exception e) {
			e.printStackTrace();
			sendMsg("Возникла ошибка при выполнении команды " + message, tags.channel);
		}
		if(user.messagesInSession!=-1) user.messagesInSession++;
		if(!tags.channel.enabled) return;
		User u = tags.channel.getUserById(user.getUserId());

		/*if(u.getBuggycoins() >= user.getBuggycoins()) */user.setBuggycoins(u.getBuggycoins());
						/*else{
							System.out.println("test");
						}*/
		if(u.messagesInSession > user.messagesInSession) user.messagesInSession = u.getBuggycoins();
		tags.channel.setUserById(user.getUserId(), user);
	}


	public static void shutdownBot() {
		Twitch.shutdown = true;
	}


	public static void onLeft(String user, String channel) {
		log.info("User "+user+" left "+channel);
	}

	public static void onJoin(String user, String channel){
		log.info("User "+user+" joined "+channel);
	}
}
