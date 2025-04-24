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

import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * Holds application logs for users
 */
public final class ApplicationLogs {
	private ApplicationLogs() {}

	public static void init() {
		try(SqlHelper sql = new SqlHelper()) {
			sql.execute("CREATE TABLE IF NOT EXISTS ApplicationLogs " +
					"(user_id bigint NOT NULL," +
					"ticket_channel bigint UNIQUE NOT NULL," +
					"poll_message_url string UNIQUE NOT NULL," +
					"date string NOT NULL DEFAULT (strftime('%e/%m/%Y')))");
		} catch(SQLException e) {
			throw new RuntimeException("Error initializing SQL", e);
		}
	}

	public static void addLog(long userId, long ticketChannelId,
	                          String pollMessageUrl) {
		try(SqlHelper sql = new SqlHelper()) {
			sql.update("INSERT INTO ApplicationLogs " +
					"(user_id,ticket_channel,poll_message_url) " +
					"VALUES (?,?,?)", smt -> {
				smt.setLong(1, userId);
				smt.setLong(2, ticketChannelId);
				smt.setString(3, pollMessageUrl);
			});
		} catch(SQLException e) {
			throw new RuntimeException("Error adding application log", e);
		}
	}

	public static int getAppCount(long userId) {
		try(SqlHelper sql = new SqlHelper()) {
			return sql.query("SELECT count() FROM ApplicationLogs WHERE user_id = ?", smt -> {
				smt.setLong(1, userId);
			}).getInt(1);
		} catch(Exception e) {
			throw new RuntimeException("Error adding application log", e);
		}
	}

	public static void listApplications(long userId, ResultSetHandler handler) {
		try(SqlHelper sql = new SqlHelper()) {
			ResultSet result = sql.query("SELECT * FROM ApplicationLogs WHERE user_id = ?", smt -> {
				smt.setLong(1, userId);
			});
			try {
				handler.handleResult(result);
			} catch(Exception e) {
				throw new RuntimeException("Error handling application log", e);
			}
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException("Error querying application logs", e);
		}
	}
}