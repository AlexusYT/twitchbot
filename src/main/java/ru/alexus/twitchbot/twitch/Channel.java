package ru.alexus.twitchbot.twitch;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import ru.alexus.twitchbot.twitch.objects.MsgTags;
import ru.alexus.twitchbot.twitch.objects.User;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;

import static ru.alexus.twitchbot.Utils.pluralizeMessage;

public class Channel {
	public String channelName;
	public String greetingMsg;
	public String goodbyeMsg;
	public boolean enabled, connectedToIRC, connectedToDB;
	public Connection connection;
	public LinkedList<String> queueToSend;
	public long firstSend;
	public int sessionId = -1;
	private final LinkedHashMap<Integer, User> userById = new LinkedHashMap<>();
	private final LinkedHashMap<String, User> userByName = new LinkedHashMap<>();
	public final LinkedList<Integer> activeUsers = new LinkedList<>();

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
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM userSession WHERE userSession.sessionId = ? AND userSession.twitchID = ?");
			preparedStatement.setInt(1, sessionId);
			preparedStatement.setInt(2, user.getUserId());
			ResultSet resultSet = preparedStatement.executeQuery();

			if(resultSet.next()) {
				user.messagesInSession = resultSet.getInt("totalMessages");
			}

			resultSet.close();
			preparedStatement.close();
		}catch (SQLException e){
			e.printStackTrace();
			return false;
		}

		if(user.messagesInSession==-1) {
			try {
				String sql = "INSERT INTO userSession (sessionId,twitchID,totalMessages) VALUES (?,?,0)";
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
			StringBuilder valuesStr = new StringBuilder();
			StringBuilder whereStr1 = new StringBuilder();
			valuesStr.append("CASE").append("\n");
			whereStr1.append("(");
			String delim = "";
			for (User user : userById.values()) {
				if(user.getBuggycoins()==0) continue;
				valuesStr.append("WHEN users.twitchID = ").append(user.getUserId()).append(" THEN ").append(user.getBuggycoins()).append("\n");

				whereStr1.append(delim).append(user.getUserId());
				delim = ",";
			}
			whereStr1.append(")");
			valuesStr.append("ELSE buggycoins").append("\n END");
			String sql = "UPDATE users SET buggycoins = "+valuesStr+" WHERE users.twitchID IN "+whereStr1;
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.execute();
		} catch (CommunicationsException e) {
			connectedToDB = false;
			startConnectingDB();
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

				valuesStr.append("WHEN userSession.sessionId = ").append(sessionId).append(" AND userSession.twitchID = ")
						.append(user.getUserId()).append(" THEN ").append(Math.max(user.messagesInSession, 0)).append("\n");

				whereStr1.append(delim).append(user.getUserId());
				delim = ",";
			}
			whereStr1.append(")");
			valuesStr.append("ELSE totalMessages").append("\n END");
			String sql = "UPDATE userSession SET totalMessages = "+valuesStr+" WHERE userSession.sessionId = ? AND userSession.twitchID IN "+whereStr1;
			//System.out.println(sql);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, sessionId);
			statement.execute();
			return true;

		} catch (CommunicationsException e) {
			connectedToDB = false;
			startConnectingDB();
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
			PreparedStatement st = connection.prepareStatement(sql);
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
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT getOrCreateSession()");
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
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users ");
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
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, sessionId);
			statement.execute();
			sessionId = -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	void startConnectingDB(){
		new Thread(() -> {
			int tries = 0;
			while (!connectedToDB) {
				if(tries>0) {
					long sec = (long) Math.pow(2, tries);
					Twitch.log.info("Retrying in " + sec + "s");
					try {
						Thread.sleep(sec * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				connectedToDB = connectToDB();
				tries++;
			}
			if(tries>1)
				Channels.sendToAll("Соединение с базой данных установлено спустя "+pluralizeMessage((tries), "попытку", "попытки","попыток") );
		}).start();
	}
	void startDisconnectingDB(){
		connectedToDB = false;
		try {

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	boolean connectToDB(){
		try {

			long lastTime = System.currentTimeMillis();
			Twitch.log.info("Connecting to database");
			DriverManager.setLoginTimeout(5);
			connection = DriverManager.getConnection(Twitch.databaseUrl+"/"+channelName+"_botDB", Twitch.databaseLogin, Twitch.databasePass);

			Twitch.log.info("Successfully connected to database "+channelName+"_botDB in "+(System.currentTimeMillis()-lastTime));
			return true;
		} catch (SQLException e) {
			Twitch.log.info("Failed to connect to database");
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
	public User getUserByName(String name) {
		return userByName.get(name.toLowerCase(Locale.ROOT));
	}
	public void setUserById(int id, User newUser) {
		userById.put(id, newUser);
		userByName.put(newUser.getDisplayName().toLowerCase(Locale.ROOT), newUser);
	}
	public void setUserByName(String name, User newUser) {
		userById.put(newUser.getUserId(), newUser);
		userByName.put(name.toLowerCase(Locale.ROOT), newUser);
	}
}
