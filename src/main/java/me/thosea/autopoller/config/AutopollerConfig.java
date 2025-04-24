/*
 * Copyright 2025 Thosea (https://github.com/imthosea)
 *
 * Licensed under the Pizzache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You should have been given a copy of the pizza license.
 * If not, you may obtain a copy of the License at
 *
 *     https://raw.githubusercontent.com/imthosea/licenses/refs/heads/master/Pizzache2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.thosea.autopoller.config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class AutopollerConfig {
	public final String token;
	public final long guildId;

	public final long ticketsCategoryId;
	public final long archivedTicketsCategoryId;

	public final long pollChannelId;
	public final long pollLengthHours;

	public final String jdbcPath;

	public final boolean sqlDebugEnabled;

	public final long cooldownSeconds;

	public final AutopollerMessages messages;

	private AutopollerConfig(Logger logger) throws Exception {
		Path propFile = Paths.get("./autopoller.properties");
		logger.info("Reading properties from {}", propFile.toAbsolutePath());

		Properties prop = new Properties();
		try(InputStream stream = Files.newInputStream(propFile)) {
			prop.load(stream);
		}

		this.token = str(prop, "token");
		this.guildId = num(prop, "guild_id");

		this.ticketsCategoryId = num(prop, "tickets_category_id");
		this.archivedTicketsCategoryId = num(prop, "archived_tickets_category_id");

		this.pollChannelId = num(prop, "poll_channel_id");
		this.pollLengthHours = num(prop, "poll_length_hours");

		this.jdbcPath = "jdbc:sqlite:" + str(prop, "db_path");

		Configurator.setRootLevel(level(prop, "logger.level_root"));
		Configurator.setLevel("me.thosea.autopoller", level(prop, "logger.level_autopoller"));
		this.sqlDebugEnabled = bool(prop, "db_debug_enabled");

		this.cooldownSeconds = num(prop, "cooldown_hours") * 60 * 60;

		this.messages = new AutopollerMessages(prop);
	}

	public static AutopollerConfig load(Logger logger) throws Exception {
		return new AutopollerConfig(logger);
	}

	public static String str(Properties prop, String key) {
		String value = prop.getProperty(key);
		if(value == null || value.isBlank())
			throw new IllegalArgumentException("Missing or blank config value for " + key);
		return value;
	}

	public static long num(Properties prop, String key) {
		try {
			return Long.parseLong(str(prop, key));
		} catch(NumberFormatException ignored) {
			throw new IllegalArgumentException("Invalid number for config value " + key);
		}
	}

	public static boolean bool(Properties prop, String key) {
		String value = str(prop, key);
		if("true".equalsIgnoreCase(value)) {
			return true;
		} else if("false".equals(value)) {
			return false;
		} else {
			throw new IllegalArgumentException("Invalid boolean for config value " + key);
		}
	}

	public static Level level(Properties prop, String key) {
		Level level = Level.valueOf(str(prop, key));
		if(level == null)
			throw new IllegalArgumentException("Invalid log level for config value " + key + ". Please see the comment.");
		return level;
	}
}