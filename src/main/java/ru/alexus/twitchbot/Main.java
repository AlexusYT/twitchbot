/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.alexus.twitchbot;

import ru.alexus.twitchbot.bot.*;

import java.util.concurrent.TimeUnit;


public class Main {

	public static void main(String[] args) throws Exception {
		TwitchBot twitchBot = new TwitchBot("daxtionoff", "oauth:ji8iylpz9yhj7tbkiw8cxsriemm2qf");
		//TwitchBot twitchBot = new TwitchBot("TheBuggyBot", "oauth:qnqb5c3by68itlapde0rh463vh5kq2");
		twitchBot.setBotEvents(new IBotEvents() {
			@Override
			public void onWhisper(TwitchUser user, TwitchWhisper message) {
				System.out.println(user.getDisplayName()+" whispered: "+message.getText());
			}

			@Override
			public void onBotConnectionFailure(Throwable throwable) {
				System.out.println("Bot failure: "+throwable);
			}

			@Override
			public void onBotConnectionSuccessful() {
				System.out.println("Bot connected");
			}

			@Override
			public void onBotStopped() {
				System.out.println("Bot stopped");
			}

			@Override
			public boolean onBotConnectionRetryStarted() {
				System.out.println("Reconnecting");
				return true;
			}

		});
		twitchBot.connectToTwitch();
		twitchBot.startLoop();
		twitchBot.addChannel("alexus_xx", new IChannelEvents() {
			@Override
			public void onBotChannelJoin(TwitchChannel channel) {
				System.out.println("Bot joined");
			}

			@Override
			public void onBotChannelLeave(TwitchChannel channel) {
				System.out.println("Bot left");
			}

			@Override
			public void onBotChannelJoinFailed(TwitchChannel channel, String reason) {
				System.out.println("Failed to join. Reason: "+reason);
			}

			@Override
			public void onUserJoin(TwitchChannel channel, String user) {
				System.out.println(user+" joined "+channel.getChannelName());
			}

			@Override
			public void onUserLeft(TwitchChannel channel, String user) {
				System.out.println(user+" left "+channel.getChannelName());
			}

			@Override
			public void onMessage(TwitchChannel channel, TwitchUser user, TwitchMessage message) {
				System.out.println(user+" sent message to "+channel.getChannelName()+": "+message.getText());
			}
		});

/*
		Thread twitchThread = new Thread(Twitch::startBot);
		twitchThread.start();
		Thread webThread = new Thread(Web::startWeb);
		webThread.start();*/
	}

}
