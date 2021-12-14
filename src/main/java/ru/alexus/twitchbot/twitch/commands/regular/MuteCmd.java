package ru.alexus.twitchbot.twitch.commands.regular;

import org.thymeleaf.standard.processor.StandardRefAttributeTagProcessor;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.twitch.Channel;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.CommonMessages;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.commands.EnumAccessLevel;
import ru.alexus.twitchbot.twitch.commands.MainCommandInfo;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

public class MuteCmd extends MainCommandInfo {
	@Override
	public CommandResult execute(CommandInfo command, String text, String[] args, MsgTags tags, Channel channel, User caller, CommandResult result) {
		if(args[0].isEmpty()) return super.execute(command, text, args, tags, channel, caller, result);
		User target = channel.getUserByName(args[0]);
		if(target==null){
			result.resultMessage = CommonMessages.userNotFound(args[0]);
			return result;
		}
		if(!target.isMutableByOthers()){
			result.resultMessage = "{.caller}, к твоему сожалению, у "+target.getDisplayName()+" в данный момент стоит запрет на мут";
			return result;
		}
		switch (target.getLevel()){
			case OWNER: result.resultMessage = "{.caller}, я не могу поднять руку на своего создателя"; return result;
			case BROADCASTER: result.resultMessage = "{.caller}, не трогать стримера!"; return result;
			case MODER: result.resultMessage = "{.caller}, модератора замутить не получится. А вот он может :D"; return result;
		}
		result = channel.checkSufficientCoins(caller, command);
		if(!result.sufficientCoins) return result;
		Twitch.sendMute(target, channel, Utils.random.nextInt(3*60)+60,
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
	public int getCoinCost(EnumAccessLevel level) {
		return 5000;
	}

	@Override
	public long getUserMaxCalls() {
		return 5;
	}

	@Override
	public long getUserCooldown() {
		return 30*60;
	}
}
