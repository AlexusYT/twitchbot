package ru.alexus.twitchbot.bot;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;

import javax.security.sasl.AuthenticationException;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TwitchBot {
	private static final String serverHostname = "irc.twitch.tv";
	private static final int serverPort = 6667;
	private static final LinkedList<String> viewerBots = new LinkedList<>();
	private static final int maxRunningThreads = 20;


	private final String botUsername;
	private final String botOauth;

	private BufferedWriter output;
	private BufferedReader input;
	private Socket socket;

	private long lastPingTime;
	private int runningThreads;
	private final LinkedHashMap<String, TwitchChannel> leftChannels = new LinkedHashMap<>();
	private final LinkedHashMap<String, TwitchChannel> joinedChannels = new LinkedHashMap<>();
	private final LinkedHashMap<String, TwitchChannel> pendingChannels = new LinkedHashMap<>();
	private final LinkedHashMap<String, TwitchChannel> sentChannels = new LinkedHashMap<>();
	private boolean botFailure = false;
	private boolean botStop = false;
	private boolean botStopped = false;

	private boolean ignoreViewerBots = true;
	private boolean printOut;
	private IBotEvents botEvents;

	static {
		new Thread(() -> {
			while (true) {
				try {
					URLConnection connection = new URL("https://api.twitchinsights.net/v1/bots/online").openConnection();
					JSONParser parser = new JSONParser(connection.getInputStream());
					Object obj = parser.parseObject().get("bots");
					if (obj instanceof ArrayList) {
						viewerBots.clear();
						for (Object bot : (ArrayList<?>) obj) {
							if (bot instanceof ArrayList) viewerBots.add((String) ((ArrayList<?>) bot).get(0));
						}
					}

				} catch (ConnectException ignored) {
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
				try {
					TimeUnit.MINUTES.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "Viewer bots updater").start();
	}


	public TwitchBot(String botUsername, String botOauth) {
		this.botUsername = botUsername.toLowerCase(Locale.ROOT);
		if (botOauth.startsWith("oauth:")) this.botOauth = botOauth;
		else this.botOauth = "oauth:" + botOauth;
	}

	public boolean connectToTwitch(int maxTries) {
		int tries = 0;
		do {
			try {
				connectToTwitch();
				return true;
			} catch (IOException e) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException ignored) {
				}
				tries++;
			}
		} while (tries < maxTries || maxTries <= 0);
		return false;
	}

	public void connectToTwitch() throws IOException {
		do {
			lastPingTime = -1;
			botStop = false;
			botStopped = false;
			if (socket != null) socket.close();
			socket = new Socket();
			socket.connect(new InetSocketAddress(serverHostname, serverPort));
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			send("PASS " + botOauth);
			send("NICK " + botUsername);
			output.flush();
			String lines = input.readLine();
			if (lines == null) continue;
			if (lines.contains("Login authentication failed")) {
				IOException exception = new AuthenticationException("Twitch login authentication failed");
				if (botEvents != null) botEvents.onBotConnectionFailure(this, exception);
				else throw exception;
			}
			if (!lines.contains("Welcome, GLHF!")) {
				IOException exception = new AuthenticationException("Unknown error occurred");
				if (botEvents != null) botEvents.onBotConnectionFailure(this, exception);
				else throw exception;
			}
			for (int i = 0; i < 6; i++) input.readLine();

			break;
		} while (true);

		send("CAP REQ :twitch.tv/membership twitch.tv/commands twitch.tv/tags");
		output.flush();
		socket.setSoTimeout(2000);
		boolean capabilitiesSet = false;
		for (int i = 0; i < 10; i++) {
			String s = input.readLine();
			if (printOut) System.out.println(s);
			if (s.contains("CAP * ACK")) {
				capabilitiesSet = true;
				break;
			}
		}
		socket.setSoTimeout(5 * 60 * 1000);
		if (!capabilitiesSet) {
			IOException exception = new IOException("Failed to request capabilities");
			if (botEvents != null) botEvents.onBotConnectionFailure(this, exception);
			else throw exception;
		}
		if (botEvents != null) botEvents.onBotConnectionSuccessful(this);
	}

	public void addChannel(String channelName, IChannelEvents listener) {
		if (leftChannels.containsKey(channelName)) joinChannel(channelName);
		else pendingChannels.put(channelName, new TwitchChannel(channelName, listener, this));
	}

	public void leaveChannel(String channelName) {

		try {
			output.write("PART #" + channelName + "\n");
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void joinChannel(String channelName) {

		try {
			if (!leftChannels.containsKey(channelName))
				throw new RuntimeException("Channel " + channelName + " not registered");
			output.write("JOIN #" + channelName + "\n");
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startLoop() {
		new Thread(() -> {

			while (!botStop) {
				try {
					for (Map.Entry<String, TwitchChannel> entry : pendingChannels.entrySet()) {
						String channelName = entry.getKey();
						TwitchChannel channel = entry.getValue();
						if (sentChannels.containsKey(channelName)) {
							sentChannels.remove(channelName);
							pendingChannels.remove(channelName);
							channel.listener.onBotChannelJoinFailed(this, channel, "twitch took too long to respond");
						}

						output.write("JOIN #" + channelName + "\n");
						output.flush();
						sentChannels.put(channelName, channel);
					}
				} catch (IOException e) {
					//e.printStackTrace();
				}
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		new Thread(() -> {
			while (!botStop) {
				try {
					String line;
					while ((line = input.readLine()) != null) {
						if (printOut) System.out.println(botUsername + ": " + line);
						String[] elements = line.split(" ", 5);
						if (elements[0].equals("PING")) {
							lastPingTime = System.currentTimeMillis();
							send("PONG :tmi.twitch.tv");
							output.flush();
							continue;
						}
						if (elements[1].equals("JOIN") || elements[1].equals("PART")) {
							String user = elements[0].substring(1, elements[0].indexOf("!"));
							String channelName = elements[2].substring(1);
							if (ignoreViewerBots && viewerBots.contains(user)) continue;
							if (elements[1].equals("JOIN")) {
								if (botUsername.equals(user)) {
									sentChannels.remove(channelName);
									TwitchChannel channel = pendingChannels.remove(channelName);
									if (channel == null) channel = leftChannels.get(channelName);


									if (channel == null) continue;
									joinedChannels.put(channelName, channel);
									TwitchChannel finalChannel = channel;
									new Thread(() -> checkExceedLimit(() -> finalChannel.listener.onBotChannelJoin(this, finalChannel))).start();
								} else {
									TwitchChannel channel = joinedChannels.get(channelName);
									if (channel == null) continue;
									new Thread(() -> checkExceedLimit(() -> channel.listener.onUserJoin(this, channel, user))).start();
								}
							} else {
								if (botUsername.equals(user)) {
									TwitchChannel channel = joinedChannels.remove(channelName);
									if (channel == null) continue;
									leftChannels.put(channelName, channel);
									new Thread(() -> checkExceedLimit(() -> channel.listener.onBotChannelLeave(this, channel))).start();
								} else {
									TwitchChannel channel = joinedChannels.get(channelName);
									if (channel == null) continue;
									new Thread(() -> checkExceedLimit(() -> channel.listener.onUserLeft(this, channel, user))).start();
								}
							}
							continue;
						}
						switch (elements[2]) {
							case "USERSTATE" -> {
								String channelName = elements[3].substring(1);
								TwitchChannel channel = joinedChannels.get(channelName);
								channel.setBotUser(new TwitchUser(elements[0].substring(1)));
							}
							case "ROOMSTATE" -> {
								String channelName = elements[3].substring(1);
								TwitchChannel channel = joinedChannels.get(channelName);
								if (channel == null) continue;
								channel.initRoom(elements[0].substring(1));
							}
							case "PRIVMSG" -> {
								String channelName = elements[3].substring(1);
								TwitchChannel channel = joinedChannels.get(channelName);
								if (channel == null) continue;
								TwitchMessage message = new TwitchMessage(elements[0].substring(1), elements[4].substring(1));
								new Thread(() -> checkExceedLimit(() -> channel.listener.onMessage(this, channel, message.getTwitchUser(), message))).start();
							}
							case "NOTICE" -> {
								String channelName = elements[3].substring(1);
								TwitchChannel channel = joinedChannels.get(channelName);
								if (channel == null) continue;
								new Thread(() -> checkExceedLimit(() -> channel.listener.onNotice(this, channel, elements[0].substring(8), elements[4].substring(1)))).start();
							}
							case "WHISPER" -> {
								if (botEvents == null) continue;
								TwitchWhisper whisper = new TwitchWhisper(elements[0].substring(1), elements[4].substring(1));
								new Thread(() -> checkExceedLimit(() -> botEvents.onWhisper(this, whisper.getTwitchUser(), whisper))).start();
							}
							default -> {
							}
						}

					}
				} catch (Exception e) {
					Exception exception = e;
					if (!botStop) {
						botFailure = true;
						if (botEvents != null) {

							if (exception instanceof SocketTimeoutException) {
								exception = new RuntimeException("Twitch server is not responding");
							}
							botEvents.onBotConnectionFailure(this, exception);
						}
					}
				}

				if (botFailure && !botStop) {
					int tries = 0;
					do {
						try {
							if (botEvents != null && !botEvents.onBotConnectionRetryStarted(this)) {
								botStop = true;
								break;
							}
							connectToTwitch();
							botFailure = false;
							break;
						} catch (Exception e) {
							botFailure = true;
						}
						tries++;
						try {
							TimeUnit.SECONDS.sleep((long) Math.pow(2, tries));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} while (true);

					pendingChannels.putAll(joinedChannels);
					joinedChannels.clear();

				}
			}
			botStopped = true;
			if (botEvents != null) botEvents.onBotStopped(this);
		}).start();

	}

	public String getChannelStatus(String channelName) {
		if (pendingChannels.containsKey(channelName)) return "pending";
		if (joinedChannels.containsKey(channelName)) return "joined";
		if (leftChannels.containsKey(channelName)) return "left";
		return "unknown";
	}

	public void stopBot() {
		try {
			if (botEvents != null) botEvents.onBotStopping(this);
			botStop = true;
			socket.close();
			pendingChannels.clear();
			joinedChannels.clear();
			leftChannels.clear();
			sentChannels.clear();
			while (!botStopped) TimeUnit.MILLISECONDS.sleep(500);
			botFailure = false;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isBotRunning() {
		return !botStopped;
	}

	public long getLastPingTime() {
		return lastPingTime;
	}

	private void send(String text) throws IOException {
		if (output == null) return;
		output.write(text + "\n");
	}

	public void sendToIRC(String text) throws IOException {
		if (output == null) return;
		output.write(text + "\n");
		output.flush();
	}

	public void sendWhisper(String user, String message) {
		try {
			if (botEvents != null) message = botEvents.onSendingWhisper(this, message);
			sendToIRC("PRIVMSG " + botUsername + " :/w " + user + " " + message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void checkExceedLimit(Runnable runnable) {
		if (runningThreads < maxRunningThreads) {
			runningThreads++;
			runnable.run();
			runningThreads--;
		} else
			System.err.println("Event skipped because of exceeding limit of running threads");
	}

	public IBotEvents getBotEvents() {
		return botEvents;
	}

	public void setBotEvents(IBotEvents botEvents) {
		this.botEvents = botEvents;
	}

	public void setIgnoreViewerBots(boolean ignoreViewerBots) {
		this.ignoreViewerBots = ignoreViewerBots;
	}

	public boolean isIgnoreViewerBots() {
		return ignoreViewerBots;
	}

	public void setPrintOut(boolean printOut) {
		this.printOut = printOut;
	}

	public String getBotUsername() {
		return botUsername;
	}
}
