package ru.alexus.twitchbot.web;

import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.events.Event;
import ru.alexus.twitchbot.eventsub.events.RedemptionAdd;
import ru.alexus.twitchbot.eventsub.events.StreamOnline;

public interface IEventSub {


	void onRewardRedemption(EventSubInfo subInfo, RedemptionAdd event);

	void subscriptionRevoked(EventSubInfo subInfo);

	void onStreamOnline(EventSubInfo subInfo, StreamOnline event);

	void onStreamOffline(EventSubInfo subInfo, Event event);
}
