package ru.alexus.twitchbot;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import ru.alexus.twitchbot.twitch.Twitch;

public class Globals {

	public static boolean shutdownTwitchBot = false;
	public static Logger log;
	public static String twitchClientId = "cxxcdpgmikulrcqf6wb899qxgfgrkw";
	public static String twitchSecret = "9fubbi0620woz6c65kf1cag7u2c4ao";


	static {
		configureLog4J();
		log = LogManager.getLogger(Twitch.class.getSimpleName());
	}


	private static void configureLog4J() {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

		// configure a console appender
		//"[%d{HH:mm:ss.SSS}][%t][%logger{6}][%-5level]: %msg%n"
		builder.add(
				builder.newAppender("stdout", "Console")
						.add(
								builder.newLayout(PatternLayout.class.getSimpleName())
										.addAttribute("pattern", "[%d{HH:mm:ss.SSS}][%t][%level]: %msg%n")
						)
		);

		builder.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("stdout")));

		Configurator.initialize(builder.build());

	}
}
