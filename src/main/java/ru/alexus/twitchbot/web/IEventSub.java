package ru.alexus.twitchbot.web;

import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.events.Event;
import ru.alexus.twitchbot.eventsub.events.RewardRedemption;
import ru.alexus.twitchbot.eventsub.events.StreamOnline;

public interface IEventSub {


	void onRewardRedemption(EventSubInfo subInfo, RewardRedemption event);

	void subscriptionRevoked(EventSubInfo subInfo);

	void onStreamOnline(EventSubInfo subInfo, StreamOnline event);

	void onStreamOffline(EventSubInfo subInfo, Event event);
}
