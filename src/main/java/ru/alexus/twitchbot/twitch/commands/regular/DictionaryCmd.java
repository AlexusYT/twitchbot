package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.twitch.commands.MainCommandInfo;

public class DictionaryCmd extends MainCommandInfo {

	@Override
	public String getDescription() {
		return "управлять словарём забавных слов, сказанных на стриме";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"словарь", "слово", "dictionary", "dict"};
	}
}
