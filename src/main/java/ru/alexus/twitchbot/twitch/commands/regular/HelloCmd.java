package ru.alexus.twitchbot.twitch.commands.regular;

import org.apache.commons.lang3.text.WordUtils;
import ru.alexus.twitchbot.twitch.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.objects.MsgTags;

import java.util.LinkedList;

import static ru.alexus.twitchbot.Utils.*;

public class HelloCmd implements ICommand {
	LinkedList<String> messagesRu = new LinkedList<>();
	LinkedList<String> messagesEn = new LinkedList<>();
	public HelloCmd(){
		messagesRu.add("Привет тебе, {.caller}");
		messagesRu.add("Добрый день, {.caller}");
		messagesRu.add("Добрый вечер, {.caller}");
		messagesRu.add("Доброе утро, {.caller}");
		messagesRu.add("{.caller} пришел, йееей! HeyGuys");
		messagesRu.add("{.caller} поздоровался со мной. Отвечу-ка я ему взаимностью");

		messagesEn.add("Оу, да вы и Англии, {.caller}. Ну что-ж, {alias} тебе");
		messagesEn.add("{Alias} my friend, {.caller}. How are you?");

	}
	@Override
	public String execute(CommandInfo alias, String text, MsgTags tags) {
		LinkedList<String> messages = isRussian(alias.calledAlias) ? messagesRu : messagesEn;
		if(!text.isEmpty())
			return getRandomText(messages).replaceAll("\\{\\.caller}", WordUtils.capitalizeFully(text));
		return getRandomText(messages);
	}

	@Override
	public String getDescription() {
		return "поздоровться с ботом";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"hello", "hi", "privet", "привет", "ку"};
	}
}
