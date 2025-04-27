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

import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * Tracked polls
 */
@Log4j2
public final class TrackedPolls {
	private TrackedPolls() {}

	public static void init() {
		try(SqlHelper sql = new SqlHelper()) {
			sql.execute("CREATE TABLE IF NOT EXISTS TrackedPolls " +
					"(applicant_id bigint NOT NULL," +
					"applicant_username string NOT NULL," +
					"poll_channel bigint NOT NULL," +
					"poll_message bigint UNIQUE NOT NULL," +
					"ticket_channel bigint UNIQUE NOT NULL," +
					"thread_channel bigint UNIQUE NOT NULL," +
					"expire_at bigint NOT NULL)");
		} catch(SQLException e) {
			throw new RuntimeException("Error initializing SQL", e);
		}
	}

	public static void cleanup() {
		try(SqlHelper sql = new SqlHelper()) {
			int rows = sql.update("DELETE FROM TrackedPolls WHERE (expire_at < unixepoch())");
			LOGGER.info("Cleaned up {} rows", rows);
		} catch(SQLException e) {
			throw new RuntimeException("Error doign SQL cleanup", e);
		}
	}

	public static void trackPoll(long applicantId, String applicantName,
	                             long pollChannelId, long pollMessageId,
	                             long ticketChannelId, long threadChannelId,
	                             long expireAtSeconds) {
		try(SqlHelper sql = new SqlHelper()) {
			sql.update("INSERT INTO TrackedPolls " +
					"(applicant_id,applicant_username,poll_channel,poll_message,ticket_channel,thread_channel,expire_at) " +
					"VALUES (?,?,?,?,?,?,?)", smt -> {
				smt.setLong(1, applicantId);
				smt.setString(2, applicantName);
				smt.setLong(3, pollChannelId);
				smt.setLong(4, pollMessageId);
				smt.setLong(5, ticketChannelId);
				smt.setLong(6, threadChannelId);
				smt.setLong(7, expireAtSeconds);
			});
		} catch(SQLException e) {
			throw new RuntimeException("Error adding poll tracker entry", e);
		}
	}

	public static void dropEntry(long channelId, long messageId) {
		try(SqlHelper sql = new SqlHelper()) {
			sql.update("DELETE FROM TrackedPolls WHERE poll_channel = ? AND poll_message = ?", smt -> {
				smt.setLong(1, channelId);
				smt.setLong(2, messageId);
			});
		} catch(Exception e) {
			throw new RuntimeException("Error dropping tracked poll entry", e);
		}
	}

	public static TrackedPollEntry getEntry(long channelId, long messageId) {
		try(SqlHelper sql = new SqlHelper()) {
			ResultSet result = sql.query("SELECT " +
					"applicant_id,applicant_username,ticket_channel,thread_channel " +
					"FROM TrackedPolls " +
					"WHERE poll_channel = ? AND poll_message = ? " +
					"AND (expire_at >= unixepoch())", smt -> {
				smt.setLong(1, channelId);
				smt.setLong(2, messageId);
			});
			if(!result.next()) {
				return null;
			}

			return new TrackedPollEntry(
					result.getLong("applicant_id"),
					result.getString("applicant_username"),
					result.getLong("ticket_channel"),
					result.getLong("thread_channel")
			);
		} catch(Exception e) {
			throw new RuntimeException("Error getting tracked poll", e);
		}
	}

	public record TrackedPollEntry(long applicantId, String applicantUsername,
	                               long ticketChannelId, long threadChannelId) {}
}