package ru.alexus.twitchbot.twitch.commands;

public class CommandResult {
	public String resultMessage;
	public int coinCost;
	public boolean sufficientCoins;

	public CommandResult() {
		resultMessage = null;
		coinCost = 0;
		sufficientCoins = true;
	}

	public CommandResult(String resultMessage, int coinCost) {
		this.resultMessage = resultMessage;
		this.coinCost = coinCost;
	}
}
