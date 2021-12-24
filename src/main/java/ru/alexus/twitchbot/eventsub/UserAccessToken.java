package ru.alexus.twitchbot.eventsub;

import org.json.JSONObject;

import java.util.LinkedList;

public class UserAccessToken {
	private final String refresh;
	private final String token;
	private final LinkedList<String> scopes = new LinkedList<>();
	private final String tokenType;

	public UserAccessToken(JSONObject root) {
		refresh = root.getString("refresh_token");
		token = root.getString("access_token");
		for (Object scope : root.getJSONArray("scope")) scopes.add((String) scope);
		tokenType = root.getString("token_type");
	}

	public String getRefreshToken() {
		return refresh;
	}


	@Override
	public String toString() {
		return tokenType + " " + token;
	}

	public String getToken() {
		return token;
	}

	public String getTokenType() {
		return tokenType;
	}

	public boolean containsScope(String scope) {
		return scopes.contains(scope);
	}
}
