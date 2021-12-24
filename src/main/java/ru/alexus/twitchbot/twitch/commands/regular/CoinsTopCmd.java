package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;

import java.util.*;

public class CoinsTopCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		int topCout = 5;
		List<BotUser> top = new LinkedList<>(botChannel.getUsersById().values());
		try{
			topCout = Math.min(Math.max(Integer.parseInt(args[0]), 1), Math.min(5, top.size()));
		}catch (Exception ignored){}
		top.sort(Comparator.comparingInt(BotUser::getCoins));
		if(topCout==1){
			BotUser user = top.get(top.size()-1);
			result.resultMessage = "Самый богатый человек на этом канале: "+user.getDisplayName()+" - "+Utils.pluralizeCoin(user.getCoins());
			return result;
		}

		StringBuilder builder = new StringBuilder();
		String delim = "";
		for (int i = 0; i < top.size(); i++) {
			if(i+1>topCout) break;
			BotUser user = top.get(top.size()-i-1);
			builder.append(delim).append(i+1).append(") ").append(user.getDisplayName()).append(" - ");
			builder.append(Utils.pluralizeCoin(user.getCoins()));
			delim = " | ";
		}
		result.resultMessage = "Топ "+topCout+" самых богатых пользователей на этом канале: "+builder;
		return result;
	}

	/*@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		int topCout = 5;
		List<User> top = new LinkedList<>(tags.channel.getUsersById().values());
		try{
			topCout = Math.min(Math.max(Integer.parseInt(text), 1), Math.min(5, top.size()));
		}catch (Exception ignored){}
		top.sort(Comparator.comparingInt(User::getBuggycoins));
		if(topCout==1){
			User user = top.get(top.size()-1);
			return "Самый богатый человек на этом канале: "+user.getDisplayName()+" - "+Utils.pluralizeMessageCoin(user.getBuggycoins());
		}
		StringBuilder builder = new StringBuilder();
		String delim = "";
		for (int i = 0; i < top.size(); i++) {
			if(i+1>topCout) break;
			User user = top.get(top.size()-i-1);
			builder.append(delim).append(i+1).append(") ").append(user.getDisplayName()).append(" - ");
			builder.append(Utils.pluralizeMessageCoin(user.getBuggycoins()));
			delim = " | ";
		}
		return "Топ "+topCout+" самых богатых пользователей на этом канале: "+builder;
	}*/

	@Override
	public String getDescription() {
		return "отобразить топ 5 пользователей по количеству коинов на счету";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"top", "топ"};
	}
}
