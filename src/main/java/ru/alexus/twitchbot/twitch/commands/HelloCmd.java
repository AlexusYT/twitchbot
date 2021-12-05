package ru.alexus.twitchbot.twitch.commands;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import ru.alexus.twitchbot.twitch.ICommand;
import ru.alexus.twitchbot.twitch.MsgTags;
import ru.alexus.twitchbot.twitch.Twitch;

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
	public String execute(Twitch twitch, String alias, String text, MsgTags tags) {
		LinkedList<String> messages = isRussian(alias) ? messagesRu : messagesEn;
		if(!text.isEmpty())
			return getRandomText(messages).replaceAll("\\{\\.caller}", WordUtils.capitalizeFully(text));
		return getRandomText(messages);
	}
}
