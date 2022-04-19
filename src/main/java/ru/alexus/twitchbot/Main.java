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

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import ru.alexus.twitchbot.bot.TwitchBot;
import ru.alexus.twitchbot.twitch.BotChannel;
import ru.alexus.twitchbot.twitch.BotConfig;
import ru.alexus.twitchbot.twitch.Database;
import ru.alexus.twitchbot.twitch.Twitch;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.security.*;

public class Main {

	public static void main(String[] args) throws Exception {
		Utils.init();
		if (Utils.isWebHost())
			System.out.println("Running on hosting");
		else
			System.out.println("Running on local");



	/*	Globals.log.info("Connection successful");

		TwitchBot twitchBot = new TwitchBot("TheBuggyBot", "oauth:qnqb5c3by68itlapde0rh463vh5kq2");
		//TwitchBot twitchBot = new TwitchBot("Alexus_XX", "oauth:lkrfplvzsvm5ow7ehayb028onmgt8e");
		//TwitchBot twitchBot = new TwitchBot("daxtionoff", "oauth:ji8iylpz9yhj7tbkiw8cxsriemm2qf");
		Twitch twitch = new Twitch();
		twitchBot.setBotEvents(twitch);
		Runtime.getRuntime().addShutdownHook(new Thread(twitchBot::stopBot));
		do {
			try {
				twitchBot.connectToTwitch();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
			TimeUnit.SECONDS.sleep(2);
		} while (true);
		new Thread(() -> {
			final Database botDatabase = new Database(Globals.databaseUrl, "u244065_botdb", Globals.databaseLogin, Globals.databasePass);
			Globals.log.info("Connecting to bot database");
			int tries = 0;
			ResultSet set = null;
			do {
				try {
					botDatabase.connect();

					set = botDatabase.executeSelect("channels");
					while (set.next()) {
						BotChannel channel = new BotChannel(set, twitch);
						if (!channel.isActivated()) continue;
						BotConfig.botChannels.put(channel.getName(), channel);
					}
					break;
				} catch (CommunicationsException e) {
					Globals.log.error("Failed to connect to bot database");
				}catch (Exception e) {
					Globals.log.error("Failed to get channels", e);
				}finally {
					botDatabase.disconnect();
					if (set != null) {
						try {
							set.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}


				}

				if(tries<6) tries++;
				try {
					TimeUnit.SECONDS.sleep((long) Math.pow(2, tries));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);

			Globals.log.info("Successfully connected to bot database");
		}, "Bot database connection").start();
		twitchBot.startLoop();
		/*new Thread(() -> {
			while (twitchBot.isBotRunning()) {
				try {
					TimeUnit.MINUTES.sleep(20);
					if (Utils.isWebHost()) Utils.sendGet(Globals.serverAddress, null, null);
				} catch (IOException | InterruptedException ignored) {
				}
			}
		}, "Web host wake").start();*/
/*
		String port = System.getenv("PORT");
		if (port == null) port = "80";
		Web web = new Web(Integer.parseInt(port), twitch);
		web.start();
		web.unsubscribeAllEvents();
		web.subscribeChannelsEvents();*/
	}

}
