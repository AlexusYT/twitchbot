package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

import java.util.*;

public class CoinsTopCmd implements ICommand {
	@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		int topCout = 5;
		List<User> top = new LinkedList<>(tags.channel.getUsersById().values());
		try{
			topCout = Math.min(Math.max(Integer.parseInt(text), 1), Math.min(5, top.size()));
		}catch (Exception ignored){}
		top.sort(Comparator.comparingInt(User::getBuggycoins));
		if(topCout==1){
			User user = top.get(top.size()-1);
			return "Самый богатый человек на этом канале: "+user.getDisplayName()+" - "+Utils.pluralizeMessage(user.getBuggycoins(), "коин", "коина", "коинов");
		}
		StringBuilder builder = new StringBuilder();
		String delim = "";
		for (int i = 0; i < top.size(); i++) {
			if(i+1>topCout) break;
			User user = top.get(top.size()-i-1);
			builder.append(delim).append(i+1).append(") ").append(user.getDisplayName()).append(" - ");
			builder.append(Utils.pluralizeMessage(user.getBuggycoins(), "коин", "коина", "коинов"));
			delim = " | ";
		}
		return "Топ "+topCout+" самых богатых человек на этом канале: "+builder;
	}

	@Override
	public String getDescription() {
		return "отобразить топ 5 пользователей по количеству коинов на счету";
	}

	@Override
	public EnumAccessLevel getAccessLevel() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"top", "топ"};
	}
}
