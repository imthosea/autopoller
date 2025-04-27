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
package me.thosea.autopoller.data;

import lombok.extern.log4j.Log4j2;
import me.thosea.autopoller.config.AutopollerConfig;
import me.thosea.autopoller.main.AutoPoller;

import java.sql.SQLException;

/*
 * Holds application delays for users
 */
@Log4j2
public final class UserDelays {
	private UserDelays() {}

	private static final AutopollerConfig CONFIG = AutoPoller.instance().config;

	public static void init() {
		try(SqlHelper sql = new SqlHelper()) {
			sql.execute("CREATE TABLE IF NOT EXISTS UserDelays " +
					"(user_id bigint NOT NULL PRIMARY KEY," +
					"time bigint NOT NULL)");
		} catch(SQLException e) {
			throw new RuntimeException("Error initializing SQL", e);
		}
	}

	public static void cleanup() {
		try(SqlHelper sql = new SqlHelper()) {
			int rows = sql.update("DELETE FROM UserDelays WHERE (time <= unixepoch() - ?)", smt -> {
				smt.setLong(1, CONFIG.cooldownSeconds);
			});
			LOGGER.info("Cleaned up {} rows", rows);
		} catch(SQLException e) {
			throw new RuntimeException("Error doign SQL cleanup", e);
		}
	}

	public static boolean addDelay(long userId) {
		try(SqlHelper sql = new SqlHelper()) {
			return sql.update("INSERT OR REPLACE INTO UserDelays (user_id,time) " +
					"SELECT ?,unixepoch() " + // i didn't even know you could put constants here
					"WHERE NOT EXISTS " +
					"(SELECT 1 FROM UserDelays WHERE user_id = ? AND time >= unixepoch() - ?)", smt -> {
				smt.setLong(1, userId);
				smt.setLong(2, userId);
				smt.setLong(3, CONFIG.cooldownSeconds);
			}) > 0;
		} catch(Exception e) {
			throw new RuntimeException("Error adding user delays", e);
		}
	}

	public static boolean removeDelay(long userId) {
		try(SqlHelper sql = new SqlHelper()) {
			return sql.update("DELETE FROM UserDelays WHERE user_id = ?", smt -> {
				smt.setLong(1, userId);
			}) > 0;
		} catch(Exception e) {
			throw new RuntimeException("Error removing user delays", e);
		}
	}
}