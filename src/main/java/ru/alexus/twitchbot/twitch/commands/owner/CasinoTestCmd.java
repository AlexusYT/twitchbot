package ru.alexus.twitchbot.twitch.commands.owner;

import ru.alexus.twitchbot.twitch.Channel;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.ICommand;
import ru.alexus.twitchbot.twitch.commands.regular.CasinoCmd;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class CasinoTestCmd implements ICommand {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		int tries = 2;
		try{
			tries = Math.max(Math.min(Integer.parseInt(args[0]), 5), 1);
		}catch (Exception ignored){}
		double count = 10000;
		try{
			count = Math.max(Math.min(Integer.parseInt(args[1]), 10000000), 10000);
		}catch (Exception ignored){}
		StringBuilder builder = new StringBuilder();
		for (int j = 0; j < tries; j++) {
			int x0 = 0;
			int x1 = 0;
			int x2 = 0;
			int x50 = 0;
			for (int i = 0; i < count; i++) {
				switch (CasinoCmd.runRandom(100)){
					case -100: x0++; break;
					case 0: x1++; break;
					case 200: x2++; break;
					case 5000: x50++; break;
				}
			}
			builder.append("TEST #").append(j+1).append(": ");
			builder.append("x0 - ").append(Math.floor(x0/count*100000)/1000).append("% | ");
			builder.append("x1 - ").append(Math.floor(x1/count*100000)/1000).append("% | ");
			builder.append("x2 - ").append(Math.floor(x2/count*100000)/1000).append("% | ");
			builder.append("x50 - ").append(Math.floor(x50/count*100000)/1000).append("%; ");
		}
		result.resultMessage = builder.toString();
		return result;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"casinotest"};
	}
}
