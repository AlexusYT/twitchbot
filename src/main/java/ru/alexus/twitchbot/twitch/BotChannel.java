package ru.alexus.twitchbot.twitch;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.bot.*;
import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.events.Event;
import ru.alexus.twitchbot.eventsub.events.RewardRedemption;
import ru.alexus.twitchbot.eventsub.events.StreamOnline;
import ru.alexus.twitchbot.eventsub.objects.Reward;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandManager;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.web.IEventSub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static ru.alexus.twitchbot.Utils.replaceVar;

public class BotChannel implements IChannelEvents, IEventSub {
	private final HashMap<String, BotUser> userByName = new HashMap<>();
	private final HashMap<Integer, BotUser> userById = new HashMap<>();
	private final LinkedList<Integer> activeUsers = new LinkedList<>();
	public final HashMap<CommandInfo, Long> commandsLastExecute = new HashMap<>();
	public final HashMap<CommandInfo, Integer> commandsExecuteCount = new HashMap<>();
	private final LinkedHashMap<String, EventSubInfo> events = new LinkedHashMap<>();

	private TwitchChannel twitchChannel;
	private final Twitch twitch;
	private final TwitchBot streamerBot;
	private final Database database;
	private final String name;
	private final String token;
	private final String greetMsg;
	private final String byeMsg;
	private final int twitchID;
	private int sessionId = -1;
	private int deathCounter;
	private final boolean activated;
	private boolean enabled;
	private boolean enableAfterJoin;
	private boolean finishingStream;
	public BotChannel(String name) {
		this.twitch = null;
		this.name = name.toLowerCase(Locale.ROOT);
		this.activated = true;
		this.twitchID = -1;
		this.token = null;
		this.greetMsg = "Привет всем";
		this.byeMsg = "Всем спасибо и пока...";
		database = new Database(Globals.databaseUrl, "u244065_botdb_"+name, Globals.databaseLogin, Globals.databasePass);
		streamerBot = null;

	}

	public BotChannel(@NotNull ResultSet set, Twitch twitch) throws SQLException {
		this.twitch = twitch;
		this.name = set.getString("name").toLowerCase(Locale.ROOT);
		this.activated = set.getBoolean("activated");
		this.twitchID = set.getInt("twitchID");
		this.token = set.getString("token");
		this.greetMsg = set.getString("greetMsg");
		this.byeMsg = set.getString("byeMsg");
		for (Object obj : new JSONArray(set.getString("events"))) {
			if (obj instanceof String event) events.put(event, null);
		}
		database = new Database(Globals.databaseUrl, "u244065_botdb_"+name, Globals.databaseLogin, Globals.databasePass);
		streamerBot = new TwitchBot(this.name, this.token);
	}

	@Override
	public void onBotChannelJoin(TwitchBot bot, TwitchChannel twitchChannel) {
		this.twitchChannel = twitchChannel;
		new Thread(() -> {
			Globals.log.info("Connecting to "+twitchChannel.getChannelName() + " database");
			int tries = 0;
			do {
				try {
					database.connect();
					break;
				} catch (CommunicationsException e) {
					Globals.log.error("Failed to connect to " + twitchChannel.getChannelName() + " database");
				}catch (Exception e) {
					Globals.log.error("Failed to connect to " + twitchChannel.getChannelName() + " database", e);
				}
				if(tries<7) tries++;
				try {
					TimeUnit.SECONDS.sleep((long) Math.pow(2, tries));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);

			Globals.log.info("Connection to " + twitchChannel.getChannelName() + " database successful");
		}, twitchChannel.getChannelName()+" database connection").start();
	}

	@Override
	public void onBotChannelLeave(TwitchBot bot, TwitchChannel twitchChannel) {
		saveData();
		database.disconnect();
		System.out.println("Bot left " + twitchChannel.getChannelName());
	}


	@Override
	public void onMessage(TwitchBot bot, TwitchChannel twitchChannel, TwitchUser twitchUser, TwitchMessage message) {
		Globals.log.info(twitchUser + " sent message to " + twitchChannel.getChannelName() + ": " + message.getText());
		System.out.println(twitchUser + " sent message to " + twitchChannel.getChannelName() + ": " + message.getText());
		BotUser user = updateUser(twitchUser);
		if (message.isFirstMsg()) {
			twitchChannel.sendMessage("Чатик, поздоровайтесь с {.caller}. Он первый раз на нашем канале!", message);
		} else if (isEnabled() && user.getMessagesInSession() == 1 && !user.isBroadcaster() && !user.isOwner()) {

			CommandInfo commandInfo = CommandManager.getCommand("привет");
			CommandResult result = commandInfo.executor.execute(commandInfo, "", new String[]{""}, message, this, user, new CommandResult());
			twitchChannel.sendMessage(result.resultMessage, message);
		}
		String msg;
		try {
			msg = CommandManager.executeCommand(message, this, user);
		} catch (Exception e) {
			e.printStackTrace();
			msg = "Возникла ошибка при выполнении команды: " + message;
		}

		if ((msg == null || msg.isEmpty()) && message.getText().length() > 1 && enabled) {
			if (Utils.converter.isNeedToConvert(message.getText())) {
				msg = "Видимо, {.caller} хотел сказать \"" + Utils.converter.mirrorLayout(message.getText()) + "\"";
			}
		}
		this.sendMessage(msg, message, user);

	}


	@Override
	public void onBotChannelJoinFailed(TwitchBot bot, TwitchChannel twitchChannel, String reason) {
		System.out.println("Failed to join. Reason: " + reason);
	}

	@Override
	public void onUserJoin(TwitchBot bot, TwitchChannel twitchChannel, String user) {
		System.out.println(user + " joined " + twitchChannel.getChannelName());
	}

	@Override
	public void onUserLeft(TwitchBot bot, TwitchChannel twitchChannel, String user) {

		System.out.println(user + " left " + twitchChannel.getChannelName());
	}

	@Override
	public String onSendingMessage(TwitchBot bot, TwitchChannel twitchChannel, String message) {
		if (message != null)
			System.out.println("Sending message to " + twitchChannel.getChannelName() + ": " + message);
		return message;
	}


	public synchronized BotUser updateUser(@NotNull TwitchUser newUser) {
		BotUser old = userById.remove(newUser.getUserId());
		if (old == null) {
			old = new BotUser(newUser, null);
			if (enabled) {
				String sql = "INSERT INTO users (id,nickname,buggycoins,twitchID,firstDate) VALUES (NULL,?,0,?,CURRENT_TIMESTAMP)";
				database.execute(sql, newUser.getDisplayName(), newUser.getUserId());
			}
		} else {
			userByName.remove(old.getLogin());
			old.copyFrom(newUser);
			if (old.getMessagesInSession() == -1) {
				String sql = "INSERT INTO userSession (sessionId,twitchID,totalMessages) VALUES (?,?,0)";
				database.execute(sql, sessionId, old.getUserId());
				old.setMessagesInSession(0);
			}
		}
		if (old.getMessagesInSession() != -1) old.incMessagesInSession();

		if (enabled) {
			userById.put(old.getUserId(), old);
			userByName.put(old.getLogin(), old);
			if (!activeUsers.contains(old.getUserId())) activeUsers.add(old.getUserId());
		}
		return old;
	}


	public void sendMessage(String message) {
		sendMessage(message, null, null);
	}

	public void sendMessage(String message, @Nullable TwitchMessage twitchMessage, @Nullable BotUser user) {
		if (user != null) message = replaceVar("coins", Utils.pluralizeCoin(user.getCoins()), message);
		if (this.twitchChannel == null) return;
		this.twitchChannel.sendMessage(message, twitchMessage);
	}

	public BotChannel getChannelByName(String name) {
		if (twitch == null) return null;
		return twitch.getChannelByName(name);
	}

	public boolean joinChannel(String name) {
		if (twitch == null) return false;
		return twitch.joinChannel(name);
	}

	public void leaveChannel(String name) {
		if (twitch == null) return;
		twitch.leaveChannel(name);
	}

	public void mute(TwitchUser user, int time, String reason) {
		if (this.twitchChannel == null) return;
		twitchChannel.mute(user, time, reason);
	}

	public void stopBot() {
		if (twitch == null) return;
		twitch.stopBot();
	}

	public String getName() {
		return name;
	}

	public boolean isActivated() {
		return activated;
	}

	public String getToken() {
		return token;
	}

	public String getGreetMsg() {
		return greetMsg;
	}

	public String getByeMsg() {
		return byeMsg;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	@Override
	public String toString() {
		return name;
	}

	public void saveData() {

		saveUsers();
		saveMsgCount();
	}

	public synchronized void saveUsers() {

		/*
		UPDATE table SET Col1 = CASE id
                      WHEN 1 THEN 1
                      WHEN 2 THEN 2
                      WHEN 4 THEN 10
                      ELSE Col1
                    END
         WHERE id IN (1, 2, 3, 4);
		 */
		StringBuilder coinsValues = new StringBuilder();
		StringBuilder mutableValues = new StringBuilder();
		StringBuilder whereStr = new StringBuilder();
		coinsValues.append("CASE").append("\n");
		mutableValues.append("CASE").append("\n");
		whereStr.append("(");
		String delim = "";
		for (BotUser user : userById.values()) {
			//if(user.getBuggycoins()==0) continue;
			coinsValues.append("WHEN users.twitchID = ").append(user.getUserId()).append(" THEN ").append(user.getCoins()).append("\n");
			mutableValues.append("WHEN users.twitchID = ").append(user.getUserId()).append(" THEN ").append(user.isMutable() ? 1 : 0).append("\n");

			whereStr.append(delim).append(user.getUserId());
			delim = ",";
		}
		if (delim.isEmpty()) return;
		whereStr.append(")");
		coinsValues.append("ELSE buggycoins").append("\n END");
		mutableValues.append("ELSE mutable").append("\n END");
		String sql = "UPDATE users SET buggycoins = " + coinsValues + ", mutable = " + mutableValues + " WHERE users.twitchID IN " + whereStr;
		database.execute(sql);
	}

	public void saveMsgCount() {
		StringBuilder valuesStr = new StringBuilder();
		StringBuilder whereStr1 = new StringBuilder();
		valuesStr.append("CASE").append("\n");
		whereStr1.append("(");
		String delim = "";
		for (BotUser user : userById.values()) {
			if (user.getMessagesInSession() == -1) continue;
			valuesStr.append("WHEN userSession.sessionId = ").append(sessionId).append(" AND userSession.twitchID = ")
					.append(user.getUserId()).append(" THEN ").append(user.getMessagesInSession()).append("\n");

			whereStr1.append(delim).append(user.getUserId());
			delim = ",";
		}
		if (delim.isEmpty()) return;
		whereStr1.append(")");
		valuesStr.append("ELSE totalMessages").append("\n END");
		//INSERT INTO table (id, name, age) VALUES(1, "A", 19) ON DUPLICATE KEY UPDATE name="A", age=19
		String sql = "UPDATE userSession SET totalMessages = " + valuesStr + " WHERE userSession.sessionId = ? AND userSession.twitchID IN " + whereStr1;
		database.execute(sql, sessionId);
	}


	public synchronized boolean startSession() {
		try {
			ResultSet existsSet = database.execute("SELECT sessions.id, sessions.ended FROM sessions ORDER BY sessions.id DESC LIMIT 1");
			if(existsSet==null) return false;
			existsSet.next();
			sessionId = existsSet.getInt("id");
			boolean ended = existsSet.getBoolean("ended");
			existsSet.close();
			if(ended){
				database.execute("INSERT INTO sessions (`id`, `startDate`, `endDate`, `ended`) VALUES (NULL, CURRENT_TIMESTAMP, NULL, '0')").close();
				existsSet = database.execute("SELECT LAST_INSERT_ID() AS id");
				existsSet.next();
				sessionId = existsSet.getInt("id");
				existsSet.close();
			}
			/*ResultSet sessionSet = database.execute("SELECT getOrCreateSession()");
			if(sessionSet==null) return false;
			sessionSet.next();
			sessionId = sessionSet.getInt(1);
			sessionSet.close();*/

			ResultSet usersSet = database.executeSelect("users");
			while (usersSet.next()) {
				BotUser user = new BotUser(null, usersSet);
				userById.put(user.getUserId(), user);
			}
			usersSet.close();

			ResultSet msgsSet = database.executeSelect("userSession", "*", "sessionId = ?", sessionId);
			while (msgsSet.next()) {
				BotUser user = userById.get(msgsSet.getInt("twitchID"));
				if (user == null) continue;
				user.setMessagesInSession(msgsSet.getInt("totalMessages"));
				System.out.println(user);
			}
			msgsSet.close();


			activeUsers.clear();
			new Thread(() -> {
				while (sessionId != -1) {
					for (Integer userId : activeUsers) {
						BotUser user = getUserById(userId);
						double K = 1;
						if (user.isSubscriber()) {
							K++;
							K += user.getTwitchUser().getSubMonths() / 10.0;
						}
						user.addCoins((int) ((2 + (user.getMessagesInSession() / 100 * 2)) * K));
						Globals.log.info("Added coins to user " + user.getDisplayName() + ": " + user.getCoins() + ". Total messages " + user.getMessagesInSession());
					}
					activeUsers.clear();
					saveData();
					try {
						TimeUnit.MINUTES.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();

			return true;
		} catch (Exception e) {
			Globals.log.info("Failed to start session", e);
			return false;
		}
	}

	public synchronized boolean endSession() {
		try {
			saveData();
			database.execute("UPDATE sessions SET endDate = CURRENT_TIMESTAMP, ended = 1 WHERE sessions.id = ?", sessionId);
			sessionId = -1;
			return true;
		} catch (Exception e) {
			Globals.log.info("Failed to end session", e);
			return false;
		}
	}

	public HashMap<String, BotUser> getUsersByName() {
		return userByName;
	}

	public HashMap<Integer, BotUser> getUsersById() {
		return userById;
	}

	public int getSessionId() {
		return sessionId;
	}

	public BotUser getUserByName(String arg) {
		return userByName.get(arg);
	}

	public BotUser getUserById(int id) {
		return userById.get(id);
	}

	public Database getDatabase() {
		return database;
	}

	public int getDeathCounter() {
		return deathCounter;
	}

	public void incDeathCounter() {
		this.deathCounter++;
	}

	public void setDeathCounter(int deathCounter) {
		this.deathCounter = deathCounter;
	}

	public LinkedHashMap<String, EventSubInfo> getEvents() {
		return events;
	}

	public int getTwitchID() {
		return twitchID;
	}

	public EventSubInfo getEvent(String event) {
		return events.get(event);
	}


	@Override
	public void onRewardRedemption(EventSubInfo subInfo, RewardRedemption event) {

		Reward reward = event.getReward();
		if (reward.getId().equals("044a7ea1-8e62-476d-b58a-30b215a778cd")) {//buy coins
			BotUser user = getUserById(event.getUserId());
			if (user == null) return;
			int value = reward.getCost() / 2;
			for (String str : reward.getPrompt().split(" ")) {
				try {
					value = Integer.parseInt(str);
				} catch (Exception ignored) {
				}
			}
			user.addCoins(value);
			this.sendMessage(user.getDisplayName() + " купил " + Utils.pluralizeCoin(value) + " за " + Utils.pluralizePoints(reward.getCost()) + " канала");
		} else if (reward.getId().equals("188bcad5-82a8-4d19-a184-0c45bd5b0014")) {//vip
			if (event.getUserId() == twitchID) {
				event.cancel();
				sendMessage(event.getUserName() + ", стример не может быть ВИПом");
				return;
			}

			if (!streamerBot.connectToTwitch(10)) {
				event.cancel();
				sendMessage(event.getUserName() + ", произошла ошибка при выдаче VIP-статуса. Баллы должны вернуться через некоторое время");
				return;
			}
			streamerBot.startLoop();
			streamerBot.addChannel(this.name, new IChannelEvents() {
				@Override
				public void onNotice(TwitchBot bot, TwitchChannel twitchChannel, String msgId, String msgText) {
					if (msgId.equals("vip_success")) {
						event.fulfill();
						sendMessage(event.getUserName() + ", держи свою випку");
					} else if (msgId.equals("bad_vip_grantee_already_vip")) {
						event.cancel();
						sendMessage(event.getUserName() + ", у тебя уже есть VIP-статус");
					}
					System.out.println(msgId);
					bot.leaveChannel(twitchChannel.getChannelName());
				}

				@Override
				public void onBotChannelJoin(TwitchBot bot, TwitchChannel twitchChannel) {
					twitchChannel.sendMessage("/vip " + event.getUserLogin());
				}

				@Override
				public void onBotChannelLeave(TwitchBot bot, TwitchChannel twitchChannel) {
					bot.stopBot();
				}
			});
		}
		System.out.println("Reward " + reward.getTitle() + " redeemed");
		System.out.println("Reward " + reward.getId() + " redeemed");
	}

	@Override
	public void subscriptionRevoked(EventSubInfo subInfo) {
		System.out.println("Subscription revoked");
	}

	@Override
	public void onStreamOnline(EventSubInfo subInfo, StreamOnline event) {
		Globals.log.info("Stream " + this + " online");
		if (!this.activated) return;
		if (!finishingStream) {
			Globals.log.info("Notifying " + this + " users");
		}
		finishingStream = false;
		String type = event.getType();
		if (type.equals("live")) {
			if (!this.enabled) {
				twitch.joinChannel(event.getBroadcasterLogin());
				while (!this.twitch.getChannelStatus(name).equals("joined")){
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (this.startSession()) {
					Globals.log.info("Session started for channel " + this.getName() + " with id " + this.getSessionId());
					sendMessage(getGreetMsg(), null, null);
					enabled = true;
				} else {
					Globals.log.error("Failed to start session for channel " + this.getName());
					sendMessage("Не удалось запустить сессию", null, null);
				}
			}
		}

	}
/*

		if (enableAfterJoin) {
			enabled = true;
			if (this.startSession()) {
				Globals.log.info("Session started for channel " + this.getName() + " with id " + this.getSessionId());
			} else {
				Globals.log.error("Failed to start session for channel " + this.getName());
			}
			sendMessage(getGreetMsg(), null, null);
		}
 */
	@Override
	public void onStreamOffline(EventSubInfo subInfo, Event event) {
		if (!enabled) return;
		Globals.log.info("Stream " + this + " offline. Waiting for restart");
		finishingStream = true;
		long lastTime = System.currentTimeMillis();
		while (finishingStream) {
			if (lastTime + 5 * 60 * 1000 < System.currentTimeMillis()) {
				Globals.log.info("Stream still offline. Finishing " + this + " session");
				if (!this.isActivated()) continue;
				Globals.log.info("Saving data for channel " + this);
				this.saveData();
				if (!this.isEnabled()) continue;
				if (endSession()) Globals.log.info("Session ended for " + this);
				else Globals.log.error("Failed to end session for " + this);

				this.sendMessage(this.getByeMsg(), null, null);
				twitch.leaveChannel(event.getBroadcasterLogin());
				break;
			}
			try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
