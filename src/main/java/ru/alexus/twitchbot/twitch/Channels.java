package ru.alexus.twitchbot.twitch;

import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.shared.Channel;
import ru.alexus.twitchbot.twitch.objects.User;

import java.util.HashMap;
import java.util.Map;

public class Channels {

	private static final HashMap<String, Channel> channels = new HashMap<>();
	static {
		addChannel("alexus_xx", "Привет, чат", "Пока, чат");
		//addChannel("daxtionoff", "Приветствую всех в чате", "Всем удачи и всем пока! Squid1 Squid2 Squid3 Squid2 Squid4", false);

		new Thread(Channels::addBuggycoinsToUsers).start();
	}
	private static void addBuggycoinsToUsers() {
		while (true){
			try {
				Thread.sleep(60000);
			} catch (InterruptedException ignored) {}

			for(Channel channel : Channels.getChannels().values()){
				for (Integer userId : channel.activeUsers){
					User user = channel.getUserById(userId);
					double K = 1;
					if(user.isSubscriber()){
						K++;
						K+=user.getSubMonths()/10.0;
					}
					user.addBuggycoins((int) ((2+(user.messagesInSession/100*2))*K));
					Globals.log.info("Added coins to user "+user.getDisplayName()+": "+user.getBuggycoins()+". Total messages "+user.messagesInSession);
					channel.setUserById(user.getUserId(), user);
				}
				channel.activeUsers.clear();
				channel.saveBuggycoinsToDB();
				channel.saveTotalMessagesToDB();

			}
		}

	}
	public static Channel addChannel(String channelName){
		addChannel(channelName, "Привет всем", "Пока всем", false);
		return getChannel(channelName);
	}
	public static void removeChannel(String channelName){
		channels.remove(channelName);
	}
	private static void addChannel(String channelName, String greetingMsg, String byeMsg){
		addChannel(channelName, greetingMsg, byeMsg, false);
	}
	private static void addChannel(String channelName, String greetingMsg, String byeMsg, boolean enabledByDefault){
		Channel config = new Channel();
		config.channelName = channelName;
		config.greetingMsg = greetingMsg;
		config.goodbyeMsg = byeMsg;
		config.enabled = enabledByDefault;
		channels.put(channelName, config);
	}
	public static HashMap<String, Channel> getChannels(){
		return channels;
	}
	public static Channel getChannel(String name){
		return channels.get(name);
	}
	public static void sendGreetAll(){
		for (Map.Entry<String, Channel> channel : channels.entrySet()) {
			Channel config = channel.getValue();
			if(!config.enabled) continue;
			Twitch.sendMsg(config.greetingMsg, config);
		}
	}
	public static void sendByeAll(){
		for (Map.Entry<String, Channel> channel : channels.entrySet()) {
			Channel config = channel.getValue();
			if(!config.enabled) continue;
			Twitch.sendMsg(config.goodbyeMsg, config);
		}
	}
	public static void sendToAll(String msg){
		for (Map.Entry<String, Channel> channel : channels.entrySet()) {
			Twitch.sendMsg(msg, channel.getValue());
		}
	}
}
