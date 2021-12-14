package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.twitch.commands.MainCommandInfo;

public class CoinsCmd extends MainCommandInfo {


	@Override
	public String getDescription() {
		return "управлять своими коинами";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"coins", "coin",  "коины", "коинс", "коин"};
	}
}
