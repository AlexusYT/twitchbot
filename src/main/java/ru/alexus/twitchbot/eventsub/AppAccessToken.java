package ru.alexus.twitchbot.eventsub;

import org.json.JSONObject;

import java.util.LinkedList;

public class AppAccessToken {
	private final String token;
	private final int expiresIn;
	private final LinkedList<String> scopes = new LinkedList<>();
	private final String tokenType;

	public AppAccessToken(JSONObject root){
		token = root.getString("access_token");
		expiresIn = root.getInt("expires_in");
		for(Object scope :  root.getJSONArray("scope")) scopes.add((String) scope);
		tokenType = root.getString("token_type");
	}

	@Override
	public String toString() {
		return tokenType+" "+token;
	}

	public String getToken() {
		return token;
	}

	public String getTokenType() {
		return tokenType;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public boolean containsScope(String scope){
		return scopes.contains(scope);
	}
}
