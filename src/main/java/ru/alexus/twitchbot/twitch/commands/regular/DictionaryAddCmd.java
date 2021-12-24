package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.SubCommandInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class DictionaryAddCmd extends SubCommandInfo {

	/*@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		if(text.isEmpty()) return super.execute(alias, text, tags);
		try {
			tags.channel.executeInsert("dictionary", "twitchID,text,textHash", "?,?,MD5(text)",  tags.getUser().getUserId(), text);

		} catch (SQLIntegrityConstraintViolationException e) {
			try {
				ResultSet result = tags.channel.executeSelect("dictionary", "twitchID", "textHash = MD5(?)", text);
				result.next();
				User user = tags.channel.getUserById(result.getInt("twitchID"));
				result.close();
				return "{.caller}, \"" + text + "\" уже есть в словаре благодаря "+user.getDisplayName();
			}catch (SQLException ex){
				e.printStackTrace();
				return "{.caller}, ошибка при добавлении в словарь";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "{.caller}, ошибка при добавлении в словарь";
		}
		tags.channel.addCoins(tags.getUser(), -getCoinCost(tags.getUser().getLevel()));
		return "{.caller}, \""+text+"\" добавлено в словарь";
	}*/

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		if (text.isEmpty()) return super.execute(command, text, args, twitchMessage, botChannel, caller, result);

		try {
			result = caller.checkSufficientCoins(command);
			if (!result.sufficientCoins) return result;

			botChannel.getDatabase().executeInsert("dictionary", "twitchID,text,textHash", "?,?,MD5(text)", caller.getUserId(), text);
			result.resultMessage = "{.caller}, \"" + text + "\" добавлено в словарь";
			return result;
		} catch (SQLIntegrityConstraintViolationException e) {
			result.coinCost = 0;
			try {
				ResultSet resultSet = botChannel.getDatabase().executeSelect("dictionary", "twitchID", "textHash = MD5(?)", text);
				resultSet.next();
				BotUser user = botChannel.getUserById(resultSet.getInt("twitchID"));
				resultSet.close();
				result.resultMessage = "{.caller}, \"" + text + "\" уже есть в словаре благодаря " + user.getDisplayName();
				return result;
			} catch (SQLException ex) {
				e.printStackTrace();
				result.resultMessage = "{.caller}, ошибка при добавлении в словарь";
				return result;
			}
		} catch (SQLException e) {
			result.coinCost = 0;
			e.printStackTrace();
			result.resultMessage = "{.caller}, ошибка при добавлении в словарь";
			return result;
		}
	}


	@Override
	public String getDescription() {
		return "добавлять слова в словарь слов";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"добавить", "add"};
	}

	@Override
	public int getCoinCost(BotUser user) {
		return 50;
	}
}
