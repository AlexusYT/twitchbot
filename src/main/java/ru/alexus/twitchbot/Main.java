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
import ru.alexus.twitchbot.twitch.Database;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.web.Web;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class Main {

	public static void main(String[] args) throws Exception {

		Utils.init();

		final Database botDatabase = new Database(Globals.databaseUrl, "botDB", Globals.databaseLogin, Globals.databasePass);

		Globals.log.info("Connecting to bot database");
		int tries = 0;
		do{
			try {
				botDatabase.connect();
				break;
			}catch (Exception e){
				Globals.log.error("Failed to connect to database", e);
			}
			tries++;
			try {
				TimeUnit.SECONDS.sleep((long) Math.pow(2, tries));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while (true);
		Globals.log.info("Connection successful");

		TwitchBot twitchBot = new TwitchBot("TheBuggyBot", "oauth:qnqb5c3by68itlapde0rh463vh5kq2");
		//TwitchBot twitchBot = new TwitchBot("Alexus_XX", "oauth:lkrfplvzsvm5ow7ehayb028onmgt8e");
		//TwitchBot twitchBot = new TwitchBot("daxtionoff", "oauth:ji8iylpz9yhj7tbkiw8cxsriemm2qf");
		Twitch twitch = new Twitch(botDatabase);
		twitchBot.setBotEvents(twitch);
		Runtime.getRuntime().addShutdownHook(new Thread(twitchBot::stopBot));
		twitchBot.connectToTwitch();
		twitchBot.startLoop();
		new Thread(() -> {
			while (true) {
				try {
					TimeUnit.MINUTES.sleep(20);
					if (Utils.isWebHost()) Utils.sendGet(Globals.serverAddress, null, "");
				} catch (IOException | InterruptedException ignored) {}
			}
		}, "Web host wake").start();

		String port = System.getenv("PORT");
		if(port==null) port = "80";
		Web web = new Web(Integer.parseInt(port), twitch, botDatabase);
		web.start();
		web.unsubscribeAllEvents();
		web.subscribeChannelsEvents();
		/*Thread webThread = new Thread(Web::startWeb);
		webThread.start();*/
	}

}
