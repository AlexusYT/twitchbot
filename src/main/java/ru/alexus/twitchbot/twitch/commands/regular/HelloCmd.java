package ru.alexus.twitchbot.twitch.commands.regular;

import org.apache.commons.lang3.text.WordUtils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;

import java.util.LinkedList;

import static ru.alexus.twitchbot.Utils.getRandomText;
import static ru.alexus.twitchbot.Utils.isRussian;

public class HelloCmd implements ICommand {
	LinkedList<String> messagesRu = new LinkedList<>();
	LinkedList<String> messagesEn = new LinkedList<>();

	public HelloCmd() {
		messagesRu.add("Привет тебе, {.caller}");
		messagesRu.add("Добрый день, {.caller}");
		messagesRu.add("Добрый вечер, {.caller}");
		messagesRu.add("Доброе утро, {.caller}");
		messagesRu.add("{.caller} пришел, йееей! HeyGuys");

		messagesEn.add("Оу, да вы и Англии, {.caller}. Ну что-ж, {alias} тебе");
		messagesEn.add("{Alias} my friend, {.caller}. How are you?");

	}

	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {

		LinkedList<String> messages = isRussian(command.calledAlias) ? messagesRu : messagesEn;

		if (!args[0].isEmpty()) {
			result.resultMessage = getRandomText(messages).replaceAll("\\{\\.caller}", WordUtils.capitalizeFully(args[0]));
			return result;
		}
		result.resultMessage = getRandomText(messages);
		return result;
	}

	/*@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		LinkedList<String> messages = isRussian(alias.calledAlias) ? messagesRu : messagesEn;

		if(!text.isEmpty())
			return getRandomText(messages).replaceAll("\\{\\.caller}", WordUtils.capitalizeFully(text));
		return getRandomText(messages);
	}*/

	@Override
	public String getDescription() {
		return "поздоровться с ботом";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"hello", "hi", "privet", "привет", "ку"};
	}
}
