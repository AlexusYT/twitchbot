package ru.alexus.twitchbot.shared;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.sun.net.httpserver.HttpContext;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.alexus.twitchbot.Globals;
import ru.alexus.twitchbot.Utils;
import ru.alexus.twitchbot.eventsub.EventSubInfo;
import ru.alexus.twitchbot.eventsub.TwitchEventSubAPI;
import ru.alexus.twitchbot.twitch.Channels;
import ru.alexus.twitchbot.twitch.Twitch;
import ru.alexus.twitchbot.twitch.commands.CommandInfo;
import ru.alexus.twitchbot.twitch.commands.CommandResult;
import ru.alexus.twitchbot.twitch.objects.BadgeInfo;
import ru.alexus.twitchbot.twitch.objects.User;
import ru.alexus.twitchbot.web.Web;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

import static ru.alexus.twitchbot.Utils.pluralizeMessage;

public class Channel {
	private boolean emoteOnly;
	private int followersOnly;
	private int channelId;
	private boolean slowMode;
	private boolean subsOnly;

	public HashMap<String, EventSubInfo> subscriptions = new HashMap<>();

	public String channelName;
	public String greetingMsg;
	public String goodbyeMsg;
	public boolean enabled, connectedToIRC, connectedToDB;
	public Connection dbConnection;
	private HttpContext httpContext;
	public LinkedList<String> queueToSend;
	public long firstSend;
	public int sessionId = -1;
	private final LinkedHashMap<Integer, User> userById = new LinkedHashMap<>();
	private final LinkedHashMap<String, User> userByName = new LinkedHashMap<>();
	public final LinkedList<Integer> activeUsers = new LinkedList<>();
	public final LinkedHashMap<Integer, Long> lastUserMsg = new LinkedHashMap<>();

	public Channel(){
		enabled=false;
		connectedToIRC = false;
		connectedToDB = false;
		queueToSend = new LinkedList<>();
	}
	public Channel(String channelName, String greetingMsg, String goodbyeMsg) {
		this();
		this.channelName = channelName;
		this.greetingMsg = greetingMsg;
		this.goodbyeMsg = goodbyeMsg;
	}

	public void init(){
		new Thread(() -> {
			int tries = 0;
			while (!connectedToDB) {
				if(tries>0) {
					long sec = (long) Math.pow(2, tries);
					Globals.log.info("Retrying in " + sec + "s");
					try {
						Thread.sleep(sec * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				connectedToDB = connectToDB();
				tries++;
			}
		}).start();
/*
 */

		httpContext = Web.registerChannel(this);


	}

	public void deinit(){
		Web.unregisterChannel(httpContext);

		connectedToDB = false;
		try {

			dbConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void subscribeEvent(String type, Map<String, String> conditions) throws IOException {
		subscriptions.put(type, TwitchEventSubAPI.subscribeToEvent(type, "1", channelName+"/callback", conditions));

	}

	public void initRoomState(String str){
		for (String tag : str.split(";")) {
			String[] tagElem = tag.split("=");
			try{
				switch (tagElem[0]) {
					case "@emote-only" -> emoteOnly = tagElem[1].equals("1");
					case "followers-only" -> followersOnly = Integer.parseInt(tagElem[1]);
					case "room-id" -> channelId = Integer.parseInt(tagElem[1]);
					case "subs-only" -> subsOnly = tagElem[1].equals("1");
					case "slow" -> slowMode = tagElem[1].equals("1");
					default -> System.out.println("Unknown channel tag: " + tag);
				}
			}catch (Exception ignored){}
		}

		try {
			subscribeEvent("channel.ban", Map.of("broadcaster_user_id", String.valueOf(channelId)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addCoins(User user, int coins){
		addCoins(user.getUserId(), coins);
	}

	public void addCoins(int id, int coins){
		User target = userById.get(id);
		if(target==null) return;
		target.addBuggycoins(coins);
	}

	public void setCoins(User user, int coins){
		setCoins(user.getUserId(), coins);
	}

	public void setCoins(int id, int coins){
		User target = userById.get(id);
		if(target==null) return;
		target.setBuggycoins(coins);
	}
	public void removeCoins(User user, int coins){
		addCoins(user.getUserId(), -coins);
	}

	public void removeCoins(int id, int coins){
		addCoins(id, -coins);
	}

	/**
	 * Checks user balance
	 * @param username username to check
	 * @param coins coins value
	 * @return negative if coins insufficient, positive or 0 if sufficient, null if user not found
	 */
	@Nullable
	public Integer checkCoins(@NonNull String username, int coins){
		User user = getUserByName(username);
		if(user==null) return null;
		return checkCoins(user, coins);
	}

	/**
	 * Checks user balance
	 * @param user user object to check
	 * @param info command result with assigned cost to coinCost
	 * @return negative if coins insufficient, positive or 0 if sufficient, null if user not found
	 */
	@NonNull
	public CommandResult checkSufficientCoins(@NonNull User user, @NonNull CommandInfo info){

		CommandResult result = new CommandResult();
		result.coinCost = info.executor.getCoinCost(user.getLevel());
		Integer value = checkCoins(user.getUserId(), result.coinCost);
		result.sufficientCoins = value!=null&&value>=0;
		return result;
	}
	/**
	 * Checks user balance
	 * @param user user object to check
	 * @param coins coins value
	 * @return negative if coins insufficient, positive or 0 if sufficient, null if user not found
	 */
	@Nullable
	public Integer checkCoins(@NonNull User user, int coins){
		return checkCoins(user.getUserId(), coins);
	}

	/**
	 * Checks user balance
	 * @param id user id to check
	 * @param coins coins value
	 * @return negative if coins insufficient, positive or 0 if sufficient, null if user not found
	 */
	@Nullable
	public Integer checkCoins(int id, int coins){
		User target = userById.get(id);
		if(target==null) return null;
		return target.getBuggycoins()-coins;
	}

	public void executeInsert(String table, String fields, String valuesScheme, Object... values) throws SQLException {
		String sql = "INSERT INTO "+table+" ("+fields+") VALUES ("+valuesScheme+")";
		PreparedStatement preparedStatement = dbConnection.prepareStatement(sql);
		for (int i = 0; i < values.length; i++){
			preparedStatement.setObject(i+1, values[i]);
		}
		preparedStatement.execute();
		preparedStatement.close();
	}
	public ResultSet executeSelect(String table, String fields, String whereScheme, Object... whereValues) throws SQLException {
		String sql = "SELECT "+fields+" FROM "+table+" WHERE "+whereScheme;
		PreparedStatement preparedStatement = dbConnection.prepareStatement(sql);
		for (int i = 0; i < whereValues.length; i++){
			preparedStatement.setObject(i+1, whereValues[i]);
		}
		return preparedStatement.executeQuery();
	}
	public boolean addUserToCurrentSession(User user){
		boolean found = false;
		for (Integer activeUser : activeUsers) {
			if(activeUser == user.getUserId()){
				found = true;
				break;
			}
		}
		if(!found) activeUsers.add(user.getUserId());

		if(!connectedToDB||sessionId==-1||user.messagesInSession!=-1) return false;
		try {
			PreparedStatement preparedStatement = dbConnection.prepareStatement("SELECT * FROM userSession WHERE userSession.sessionId = ? AND userSession.twitchID = ?");
			preparedStatement.setInt(1, sessionId);
			preparedStatement.setInt(2, user.getUserId());
			ResultSet resultSet = preparedStatement.executeQuery();

			if(resultSet.next()) {
				user.messagesInSession = resultSet.getInt("totalMessages");
			}
			System.out.println(user);

			resultSet.close();
			preparedStatement.close();
		}catch (SQLException e){
			e.printStackTrace();
			return false;
		}

		if(user.messagesInSession==-1) {
			try {
				String sql = "INSERT INTO userSession (sessionId,twitchID,totalMessages) VALUES (?,?,0)";
				PreparedStatement preparedStatement = dbConnection.prepareStatement(sql);
				preparedStatement.setInt(1, sessionId);
				preparedStatement.setInt(2, user.getUserId());
				preparedStatement.execute();
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;

	}

	public boolean saveBuggycoinsToDB(){
		if(!connectedToDB || !enabled) return false;
		try {
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
			StringBuilder whereStr1 = new StringBuilder();
			coinsValues.append("CASE").append("\n");
			mutableValues.append("CASE").append("\n");
			whereStr1.append("(");
			String delim = "";
			for (User user : userById.values()) {
				if(user.getBuggycoins()==0) continue;
				coinsValues.append("WHEN users.twitchID = ").append(user.getUserId()).append(" THEN ").append(user.getBuggycoins()).append("\n");
				mutableValues.append("WHEN users.twitchID = ").append(user.getUserId()).append(" THEN ").append(user.isMutableByOthers()?1:0).append("\n");

				whereStr1.append(delim).append(user.getUserId());
				delim = ",";
			}
			whereStr1.append(")");
			coinsValues.append("ELSE buggycoins").append("\n END");
			mutableValues.append("ELSE mutable").append("\n END");
			String sql = "UPDATE users SET buggycoins = "+coinsValues+", mutable = "+mutableValues+" WHERE users.twitchID IN "+whereStr1;
			//System.out.println(sql);
			PreparedStatement statement = dbConnection.prepareStatement(sql);
			statement.execute();
		} catch (CommunicationsException e) {
			connectedToDB = false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean saveTotalMessagesToDB(){
		if(!connectedToDB || !enabled) return false;
		try {
			StringBuilder valuesStr = new StringBuilder();
			StringBuilder whereStr1 = new StringBuilder();
			valuesStr.append("CASE").append("\n");
			whereStr1.append("(");
			String delim = "";
			for (User user : userById.values()) {
				if(user.messagesInSession==-1) continue;
				valuesStr.append("WHEN userSession.sessionId = ").append(sessionId).append(" AND userSession.twitchID = ")
						.append(user.getUserId()).append(" THEN ").append(user.messagesInSession).append("\n");

				whereStr1.append(delim).append(user.getUserId());
				delim = ",";
			}
			if(delim.isEmpty()) return true;
			whereStr1.append(")");
			valuesStr.append("ELSE totalMessages").append("\n END");
			String sql = "UPDATE userSession SET totalMessages = "+valuesStr+" WHERE userSession.sessionId = ? AND userSession.twitchID IN "+whereStr1;
			//System.out.println(sql);
			PreparedStatement statement = dbConnection.prepareStatement(sql);
			statement.setInt(1, sessionId);
			statement.execute();
			return true;

		} catch (CommunicationsException e) {
			connectedToDB = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean registerUser(User user){
		if(!enabled) return false;
		if(userById.containsKey(user.getUserId())){
			return true;
		}
		userById.put(user.getUserId(), user);
		if(!connectedToDB) return false;
		try {
			String sql = "INSERT INTO users (id,nickname,buggycoins,twitchID) VALUES (NULL,?,0,?)";
			PreparedStatement st = dbConnection.prepareStatement(sql);
			st.setString(1, user.getDisplayName());
			st.setInt(2, user.getUserId());
			st.execute();
			return true;
		}catch (SQLException e){
			e.printStackTrace();
		}
		return false;
	}

	public boolean startSession(){
		if(!connectedToDB) return false;
		try {
			PreparedStatement preparedStatement = dbConnection.prepareStatement("SELECT getOrCreateSession()");
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			sessionId = resultSet.getInt(1);
			resultSet.close();
			preparedStatement.close();

		}catch (SQLException e){
			e.printStackTrace();
			return false;
		}
		try {
			PreparedStatement preparedStatement = dbConnection.prepareStatement("SELECT * FROM users ");
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()){
				User user = new User(resultSet);
				userById.put(user.getUserId(), user);

			}
			resultSet.close();
			preparedStatement.close();
		}catch (SQLException e){
			e.printStackTrace();
			return false;
		}
		return true;

	}
	public boolean endSession(){
		if(!connectedToDB || sessionId == -1) return false;
		saveBuggycoinsToDB();
		saveTotalMessagesToDB();

		try {
			String sql = "UPDATE `sessions` SET `endDate` = CURRENT_TIMESTAMP, `ended` = '1' WHERE `sessions`.`id` = ?";
			PreparedStatement statement = dbConnection.prepareStatement(sql);
			statement.setInt(1, sessionId);
			statement.execute();
			sessionId = -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void startConnectingDB(){
	}
	public void startDisconnectingDB(){
	}


	public boolean connectToDB(){
		try {

			long lastTime = System.currentTimeMillis();
			Globals.log.info("Connecting to database");
			DriverManager.setLoginTimeout(5);
			dbConnection = DriverManager.getConnection(Twitch.databaseUrl+"/"+channelName+"_botDB", Twitch.databaseLogin, Twitch.databasePass);

			Globals.log.info("Successfully connected to database "+channelName+"_botDB in "+(System.currentTimeMillis()-lastTime));
			return true;
		} catch (SQLException e) {
			Globals.log.info("Failed to connect to database");
			e.printStackTrace();
		}
		return false;
	}

	public HashMap<Integer, User> getUsersById() {
		return userById;
	}

	public User getUserById(int id) {
		return userById.get(id);
	}
	@Nullable
	public User getUserByName(@NonNull String name) {
		name = name.toLowerCase(Locale.ROOT);
		if(name.startsWith("@")) name = name.substring(1);
		return userByName.get(name);
	}

	public void setUserById(int id, @NonNull User newUser) {
		userById.put(id, newUser);
		userByName.put(newUser.getDisplayName().toLowerCase(Locale.ROOT), newUser);
	}

	public void setUserByName(@NonNull String name, @NonNull User newUser) {
		if(name.startsWith("@")) name = name.substring(1);
		userById.put(newUser.getUserId(), newUser);
		userByName.put(name.toLowerCase(Locale.ROOT), newUser);
	}

}
