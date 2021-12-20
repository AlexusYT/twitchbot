package ru.alexus.twitchbot.twitch.commands.regular;

import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.TwitchMessage;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotUser;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.CommonMessages;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.MainCommandInfo;

public class MuteCmd extends MainCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, TwitchMessage twitchMessage, BotChannel botChannel, BotUser caller, CommandResult result) {
		if(args[0].isEmpty()) return super.execute(command, text, args, twitchMessage, botChannel, caller, result);
		BotUser target = botChannel.getUserByName(args[0]);
		if(target==null){
			result.resultMessage = CommonMessages.userNotFound(args[0]);
			return result;
		}
		if(!target.isMutable()){
			result.resultMessage = "{.caller}, к твоему сожалению, у "+target.getDisplayName()+" в данный момент стоит запрет на мут";
			return result;
		}
		if(target.isOwner()){
			result.resultMessage = "{.caller}, я не могу поднять руку на своего создателя";
			return result;
		}
		if(target.isBroadcaster()){
			result.resultMessage = "{.caller}, не трогать стримера!";
			return result;
		}
		if(target.isMod()){
			result.resultMessage = "{.caller}, модератора замутить не получится. А вот он может :D";
			return result;
		}
		result = caller.checkSufficientCoins(command);
		if(!result.sufficientCoins) return result;
		botChannel.mute(target.getTwitchUser(), Utils.random.nextInt(3*60)+60,
				"Зритель "+caller.getDisplayName()+" временно отстранил вас. Чтобы запретить зрителям вас отстранять, напишите \"!"+command.calledAlias+" запретить\"");

		result.resultMessage = null;
		return result;

	}

	@Override
	public String[] getAliases() {
		return new String[]{"мут", "отстранить", "mute"};
	}

	@Override
	public String getDescription() {
		return "отстранить (замутить) указанного пользователя, который не запретил отстранять себя командой !{alias} запретить";
	}

	@Override
	public int getCoinCost(BotUser user) {
		if (user.isOwner()|| user.isBroadcaster()) return 1;
		if (user.isSubscriber()) return 2500;
		if (user.isVip()) return 4000;
		return 5000;
	}

	@Override
	public long getUserMaxCalls(BotUser user) {
		return 5;
	}

	@Override
	public long getUserCooldown(BotUser user) {
		return 30*60;
	}


}
