package ru.alexus.twitchbot.twitch;


import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandManager;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;


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
		Utils.init();
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

		Profiler.start("User registering");
		if(tags.channel.registerUser(user)){
			tags.channel.addUserToCurrentSession(user);
		}else{
			if(tags.channel.enabled)
				Twitch.log.info("Failed to add user "+user.getDisplayName());
		}
		Profiler.endAndPrint();

		log.info(user.getDisplayName()+": "+message);

		Profiler.start("First msg check");
		String msgToSend = "";
		if(tags.isFirstMsg())
			msgToSend = "Чатик, поздоровайтесь с {.caller}. Он первый раз на нашем канале!";
		else if(tags.channel.enabled&&user.messagesInSession==-1&&user.getLevel().ordinal() < EnumAccessLevel.BROADCASTER.ordinal()){
			CommandInfo commandInfo = CommandManager.getCommand("привет");
			CommandResult result = commandInfo.executor.execute(commandInfo, "", new String[]{""}, tags, tags.channel, tags.getUser(), new CommandResult());
			msgToSend = result.resultMessage;
		}
		Profiler.endAndPrint();

		Profiler.start("Executing command");
		CommandInfo cmd = null;
		try {
			cmd = CommandManager.extractCommand(message, tags);
			if(cmd==null){
			}else {
				String result = CommandManager.executeCommand(message, tags, cmd);
				if (result != null) msgToSend = result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			msgToSend = "Возникла ошибка при выполнении команды " + message;
		}
		if((msgToSend == null || msgToSend.isEmpty())&&message.length()>1&&tags.channel.enabled){
			if(Utils.converter.isNeedToConvert(message)){
				msgToSend = "Видимо, {.caller} хотел сказать \""+Utils.converter.mirrorLayout(message)+"\"";
			}
		}
		Profiler.endAndPrint();
		Profiler.start("Sending result");
		if(cmd==null)
			Twitch.sendMsgReplace(msgToSend, tags, null, true);
		else
			Twitch.sendMsgReplace(msgToSend, tags, cmd, cmd.level.ordinal()>EnumAccessLevel.SUBSCRIBER.ordinal());
		Profiler.endAndPrint();
		Profiler.start("Updating user stats");
		if(user.messagesInSession!=-1) user.messagesInSession++;
		if(!tags.channel.enabled) return;
		User u = tags.channel.getUserById(user.getUserId());

		user.setBuggycoins(u.getBuggycoins());
		user.setMutableByOthers(u.isMutableByOthers());
		if(u.messagesInSession > user.messagesInSession) user.messagesInSession = u.getBuggycoins();

		Long lastUserSendTime = tags.channel.lastUserMsg.put(user.getUserId(), System.currentTimeMillis());
		if(lastUserSendTime!=null&&lastUserSendTime+1000>System.currentTimeMillis()&&user.getLevel().ordinal() < EnumAccessLevel.BROADCASTER.ordinal()){
			user.addBuggycoins(-20);
			Twitch.log.info("User "+user.getDisplayName()+" flood detected!");
		}
		tags.channel.setUserById(user.getUserId(), user);
		Profiler.endAndPrint();
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
