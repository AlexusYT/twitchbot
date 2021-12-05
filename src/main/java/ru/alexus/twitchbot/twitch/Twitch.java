package ru.alexus.twitchbot.twitch;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

public class Twitch {
	boolean running = true;
	private static final String channelName = "alexus_xx";
	BufferedReader input;
	BufferedWriter output;
	public static void startBot(){
		System.out.println("Twitch bot thread started");
		new Twitch().run();
		System.out.println("Twitch bot thread ended");
	}
	private Twitch(){

	}
	void run() {
		while (running) {
			try {
				Socket socket = new Socket();
				//socket.setSoTimeout(5000);
				socket.connect(new InetSocketAddress("irc.twitch.tv", 6667));
				if (socket.isConnected())
					System.out.println("Connected");
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				sendToIrc("PASS oauth:qnqb5c3by68itlapde0rh463vh5kq2");
				sendToIrc("NICK the_space_bot");
				sendToIrc("JOIN #" + channelName);
				sendToIrc("JOIN #" + "daxtionoff");
				sendToIrc("CAP REQ :twitch.tv/membership twitch.tv/commands twitch.tv/tags");
				sendMsg("Привет", "alexus_xx");
				while (running) {
					String line;
					while ((line = input.readLine()) != null) {
						String[] elements = line.split(" ", 5);
						if (elements[0].equals("PING")) {
							System.out.println("Server requested ping");
							sendToIrc("PONG " + elements[1]);
							continue;
						}
						switch (elements[2]) {
							case "PRIVMSG":


								String message = elements[4].substring(1);
								MsgTags tags = new MsgTags(elements[0]);
								//System.out.println(channel+": "+tags.getDisplayName()+" sent message: "+message);
								CommandManager.executeCommand(message, tags, elements[3].substring(1), this);
								break;
							default:
								System.out.println(Arrays.toString(elements));
						}
					}
				}
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();

			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


	}

	public void sendToIrc(String text) throws IOException {
		System.out.println("Sending to IRC: "+text);
		output.write(text+"\n");
		output.flush();
	}
	public void sendMsg(String text) {
		sendMsg(text, channelName);
	}
	public void sendMsg(String text, String channel) {
		try {
			System.out.println("Sending to channel " + channel + ": " + text);
			output.write("PRIVMSG #" + channel + " :" + text + "\n");
			output.flush();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}