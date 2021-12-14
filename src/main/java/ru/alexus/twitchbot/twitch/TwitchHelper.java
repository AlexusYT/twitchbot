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
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

public class TwitchHelper {
	protected static LinkedList<String> viewerBots = new LinkedList<>();
	public static Logger log;

	private static BufferedWriter output;
	private static BufferedReader input;
	private static Socket socket;

	private static long lastPingTime = 0;
	public static boolean shutdown = false;

	static {
		configureLog4J();
		log = LogManager.getLogger(Twitch.class.getSimpleName());
	}

	protected static void botListUpdater() {
		while (true){
			try {
				URLConnection connection = new URL("https://api.twitchinsights.net/v1/bots/online").openConnection();
				JSONParser parser = new JSONParser(connection.getInputStream());
				Object obj = parser.parseObject().get("bots");
				if(obj instanceof ArrayList){
					viewerBots.clear();
					for (Object bot : (ArrayList<?>) obj) {
						if(bot instanceof ArrayList) viewerBots.add((String) ((ArrayList<?>)bot).get(0));
					}
				}

			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(60000*5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	protected static void configureLog4J() {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

		// configure a console appender
		//"[%d{HH:mm:ss.SSS}][%t][%logger{6}][%-5level]: %msg%n"
		builder.add(
				builder.newAppender("stdout", "Console")
						.add(
								builder.newLayout(PatternLayout.class.getSimpleName())
										.addAttribute("pattern", "[%d{HH:mm:ss.SSS}][%t][%level]: %msg%n")
						)
		);

		builder.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("stdout")));

		Configurator.initialize(builder.build());

	}
	protected static void run() throws IOException {


		sendToIrc("PASS oauth:qnqb5c3by68itlapde0rh463vh5kq2");
		sendToIrc("NICK TheBuggyBot");
		sendToIrc("CAP REQ :twitch.tv/membership twitch.tv/commands twitch.tv/tags");

		for (String channel : Channels.getChannels().keySet()) joinChannel(channel);

		String botNameOnServer = "";
		int errorCount = 0;
		while (!shutdown) {
			String line;
			while ((line = input.readLine()) != null) {
				errorCount = 0;
				String[] elements = line.split(" ", 5);
				if (elements[0].equals("PING")) {
					sendToIrc("PONG " + elements[1]);
					lastPingTime = System.currentTimeMillis();
					continue;
				}else if(elements[0].equals(":tmi.twitch.tv")){
					if(elements[1].equals("NOTICE")){
						System.err.println("Failed to login to Twitch IRC");
					}else {
						if(!botNameOnServer.isEmpty()) continue;
						botNameOnServer = elements[2];
						log.info("Successfully logged in to Twitch IRC");
					}
					continue;
				}
				if(elements[1].equals("JOIN")||elements[1].equals("PART")){
					String user = elements[0].substring(1, elements[0].indexOf("!"));
					String channel = elements[2].substring(1);
					if(botNameOnServer.equals(user)) {
						Channel config = Channels.getChannel(channel);
						if(config==null) config = Channels.addChannel(channel);

						config.connectedToIRC = elements[1].equals("JOIN");
						if(config.connectedToIRC){
							config.startConnectingDB();
						}else{
							config.startDisconnectingDB();
							config.enabled = false;
							Channels.removeChannel(channel);
						}
						continue;
					}
					if(viewerBots.contains(user)) continue;
					if(elements[1].equals("JOIN"))
						Twitch.onJoin(user, channel);
					else Twitch.onLeft(user, channel);

					continue;
				}
				if (elements[2].equals("PRIVMSG")) {
					MsgTags tags = new MsgTags(elements[0], elements[3].substring(1));
					User user = tags.getUser();

					if (viewerBots.contains(user.getDisplayName().toLowerCase(Locale.ROOT))) continue;

					String message = elements[4].substring(1);
					Profiler.start("onPrivMsg");
					Twitch.onPrivMsg(tags, user, message);
					Profiler.endAndPrint("onPrivMsg");
				} else {
					log.info(Arrays.toString(elements));
				}

				if (shutdown) {
					Channels.sendByeAll();
					break;
				}
				socket.setSoTimeout(0);
			}
			errorCount++;
			if(errorCount>10) throw new RuntimeException("Failed to start communicating");

		}

	}


	public static void sendToIrc(String text) throws IOException {
		log.info("Sending to IRC: "+text);

		output.write(text+"\n");
		output.flush();
	}

	public static void joinChannel(String channel) {
		try {
			log.info("Joining channel: " + channel);
			output.write("JOIN #" + channel + "\n");
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void leftChannel(String channel) {
		try {
			log.info("Leaving channel: " + channel);
			output.write("PART #" + channel + "\n");
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void sendMsgReplace(String text, MsgTags tags, CommandInfo command){
		sendMsgReplace(text, tags, command, false);
	}
	public static void sendMute(User user, Channel channel, int time, String reason) {
		sendMsg("/timeout "+user.getDisplayName()+" "+time+" "+reason, channel, true);
	}
	public static void sendMsgReplace(String text, MsgTags tags, CommandInfo command, boolean immediately){

		if(text==null||text.isEmpty()) return;
		Profiler.start("replaceVars call");
		String vars = Utils.replaceVars(text, tags, command);
		Profiler.endAndPrint();
		sendMsg(vars, tags.channel, immediately);
	}
	public static void sendMsg(String text, Channel channel){
		sendMsg(text, channel, false);
	}
	public static void sendMsg(String text, Channel channel, boolean immediately) {
		Profiler.start("sendMsg");
		if(text.isEmpty()) return;
		if(immediately){
			try {
				log.info("Sending to channel " + channel.channelName + " message immediately: " + text);
				output.write("PRIVMSG #" + channel.channelName + " :" + text + "\n");
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}else {
			Profiler.start("Adding to queue");
			if (channel.queueToSend.isEmpty()) channel.firstSend = System.currentTimeMillis();
			channel.queueToSend.add(text);
			log.info("Adding to channel " + channel.channelName + " queue: " + text);
		}
		Profiler.endAndPrint();
	}
	static void senderThread(){
		while (true){
			for (Channel channel : Channels.getChannels().values()) {
				try {
					if (!channel.enabled) continue;
					if (!channel.queueToSend.isEmpty()&&channel.firstSend + 500 < System.currentTimeMillis()) {
						StringBuilder builder = new StringBuilder();
						String delim = "";
						for (String msg : channel.queueToSend) {
							builder.append(delim).append(msg);
							delim=" | ";
						}
						if(!builder.toString().endsWith(".")) builder.append(".");
						log.info("Flushing messages to channel " + channel.channelName);
						output.write("PRIVMSG #" + channel.channelName + " :" + builder + "\n");
						channel.queueToSend.clear();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			try {
				if(output!=null) output.flush();
				Thread.sleep(20);
			} catch (InterruptedException | IOException ignored) {}

		}
	}

	protected static void connectionMonitor(){
		while (true){
			if(lastPingTime+6*60*1000<System.currentTimeMillis()){
				log.info("Twitch is not responding. Reconnecting");
				for (Channel channel : Channels.getChannels().values()){
					channel.saveTotalMessagesToDB();
					channel.saveBuggycoinsToDB();
					if(!channel.connectedToDB){
						channel.connectedToDB = channel.connectToDB();
						channel.saveTotalMessagesToDB();
						channel.saveBuggycoinsToDB();
					}
					channel.startDisconnectingDB();
					channel.connectedToIRC = false;
					channel.connectedToDB = channel.connectToDB();
				}
				lastPingTime = System.currentTimeMillis();
				disconnectFromTwitch();
				//connectToTwitch();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}



	protected static void connectToTwitch(){
		lastPingTime = System.currentTimeMillis();
		log.info("Connecting to Twitch IRC server");
		int tries = 0;
		do{
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress("irc.twitch.tv", 6667));
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				socket.setSoTimeout(5000);
				break;
			}catch (Exception e){
				System.err.println("Failed to connect to Twitch IRC server");
				disconnectFromTwitch();
			}
			tries++;
			try {
				Thread.sleep((long) Math.pow(2, tries)*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while (true);
		log.info("Successfully connected to Twitch IRC server");
	}
	protected static void disconnectFromTwitch(){

		try { if (socket != null) socket.close(); } catch (Exception ignored) {}

	}

}
