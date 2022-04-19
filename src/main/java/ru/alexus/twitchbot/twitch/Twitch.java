package ru.alexus.twitchbot.twitch;


import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.bot.IBotEvents;
import ru.alexus.twitchbot.bot.TwitchBot;
import ru.alexus.twitchbot.bot.TwitchUser;
import ru.alexus.twitchbot.bot.TwitchWhisper;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandManager;
import ru.alexus.twitchbot.twitch.commands.CommandResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;


public class Twitch implements IBotEvents {
	private TwitchBot bot;

	public Twitch() {
	}


	@Override
	public void onWhisper(TwitchBot bot, TwitchUser user, TwitchWhisper message) {
		System.out.println(user.getDisplayName() + " whispered: " + message.getText());
		if (!user.isOwner()) return;

		String[] command = message.getText().split(" ", 3);
		String alias = command[0].substring(1).toLowerCase(Locale.ROOT);

		CommandInfo cmd = CommandManager.getCommand(alias);
		if (cmd == null) return;

		String args = "";
		if (command.length == 2) args = command[1];
		else if (command.length == 3) args = command[1] + " " + command[2];
		CommandResult result = cmd.executor.execute(cmd, args, args.split(" "), message, this, user, new CommandResult());
		if (result == null || result.resultMessage == null || result.resultMessage.isEmpty()) return;
		bot.sendWhisper(user.getDisplayName(), result.resultMessage);

	}

	@Override
	public String onSendingWhisper(TwitchBot bot, String message) {
		System.out.println("Sending whisper: " + message);
		return message;
	}

	@Override
	public void onBotConnectionFailure(TwitchBot bot, Throwable throwable) {
		for (BotChannel channel : BotConfig.botChannels.values()) {
			channel.saveData();
		}
		System.out.println("Bot failure: " + throwable);
		throwable.printStackTrace();
		CommandManager.unregisterAll();
	}

	@Override
	public void onBotConnectionSuccessful(TwitchBot bot) {
		this.bot = bot;
		CommandManager.registerAll();
		Globals.log.info("Bot " + bot.getBotUsername() + " connected to Twitch");
	}

	@Override
	public void onBotStopping(TwitchBot bot) {
		System.out.println("Bot stopping");
		for (BotChannel channel : BotConfig.botChannels.values()) {
			if (!channel.isActivated()) continue;
			System.out.println("Saving data for channel " + channel);
			channel.saveData();
			if (!channel.isEnabled()) continue;
			channel.sendMessage(channel.getByeMsg(), null, null);
		}
		this.bot = bot;

		CommandManager.unregisterAll();
	}

	@Override
	public void onBotStopped(TwitchBot bot) {
		this.bot = bot;
		System.out.println("Bot stopped");
	}

	@Override
	public boolean onBotConnectionRetryStarted(TwitchBot bot) {
		this.bot = bot;
		System.out.println("Reconnecting");
		return true;
	}

	public BotChannel getChannelByName(String name) {
		return BotConfig.botChannels.get(name);
	}

	public boolean joinChannel(String name) {
		String status = bot.getChannelStatus(name);
		if(!status.equals("joined")&&!status.equals("unknown")) return false;
		BotChannel channel = BotConfig.getChannel(name);
		bot.addChannel(channel.getName(), channel);

		return true;
	}

	public void leaveChannel(String name) {
		bot.leaveChannel(name);
	}

	public void stopBot() {
		bot.stopBot();
	}

	public String getChannelStatus(String name) {
		if(bot==null) return "unknown";
		return bot.getChannelStatus(name);
	}


	/*


	public static void onPrivMsg(MsgTags tags, User user, String message){


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
			Globals.log.info("User "+user.getDisplayName()+" flood detected!");
		}
		tags.channel.setUserById(user.getUserId(), user);
		Profiler.endAndPrint();
	}

*/

}
