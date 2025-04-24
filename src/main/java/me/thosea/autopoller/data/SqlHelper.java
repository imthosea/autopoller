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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import me.thosea.autopoller.config.AutopollerConfig;
import me.thosea.autopoller.main.AutoPoller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author thosea<br>
 * this is like, the 5th project this class been copied to with slight modifications
 */
@SuppressWarnings("resource")
@Log4j2
public class SqlHelper implements AutoCloseable {
	private static final HikariDataSource HIKARI;

	private static final boolean DEBUG_ENABLED;
	private static final AtomicInteger DEBUG_COUNTER;

	static {
		AutopollerConfig config = AutoPoller.instance().config;

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(config.jdbcPath);
		HIKARI = new HikariDataSource(hikariConfig);

		DEBUG_ENABLED = config.sqlDebugEnabled;

		if(DEBUG_ENABLED) {
			DEBUG_COUNTER = new AtomicInteger();
			LOGGER.info("SQL debug is enabled! Each SqlHelper will be assigned an ID for logging.");
			LOGGER.info("Performance will be reduced.");
		} else {
			DEBUG_COUNTER = null;
			LOGGER.info("SQL debug is not enabled.");
		}
	}

	private final Connection connection;
	private final int debugId;

	/*
	 * Trackers to avoid closing manually
	 */
	protected Statement lastStatement;
	protected ResultSet lastResult;

	@Getter private boolean closed = false;

	public SqlHelper() throws SQLException {
		this.connection = HIKARI.getConnection();
		this.debugId = DEBUG_ENABLED ? DEBUG_COUNTER.incrementAndGet() : -1;
	}

	public void execute(String sql) throws SQLException {
		LOGGER.trace("Execute: {} ({})", sql, debugId);
		statement().execute(sql);
	}

	public int update(String sql) throws SQLException {
		LOGGER.trace("Update: {} ({})", sql, debugId);
		return this.statement().executeUpdate(sql);
	}

	public int update(String sql, StatementPreparer preparer) throws SQLException {
		LOGGER.trace("Prepared update: {} ({})", sql, debugId);
		PreparedStatement prepared = this.prepared(sql);
		preparer.prepare(prepared);
		return prepared.executeUpdate();
	}

	public ResultSet query(String sql) throws SQLException {
		LOGGER.trace("Query: {} ({})", sql, debugId);
		ResultSet result = statement().executeQuery(sql);
		this.lastResult = result;
		return result;
	}

	public ResultSet query(String sql, StatementPreparer preparer) throws SQLException {
		LOGGER.trace("Prepared query: {} ({})", sql, debugId);
		PreparedStatement prepared = prepared(sql);
		preparer.prepare(prepared);

		ResultSet result = prepared.executeQuery();
		this.lastResult = result;
		return result;
	}

	public PreparedStatement prepared(String sql) throws SQLException {
		checkClosed();
		clearTrackers();
		PreparedStatement prepared = connection.prepareStatement(sql);
		this.lastStatement = prepared;
		return prepared;
	}

	public Statement statement() throws SQLException {
		checkClosed();
		clearTrackers();
		Statement statement = connection.createStatement();
		this.lastStatement = statement;
		return statement;
	}

	public void clearTrackers() {
		if(lastResult != null) {
			try {
				lastResult.close();
			} catch(SQLException ignored) {}
			this.lastResult = null;
		}

		if(lastStatement != null) {
			try {
				lastStatement.close();
			} catch(SQLException ignored) {}
			this.lastStatement = null;
		}
	}

	public void setTrackedStatement(Statement statement) {
		this.clearTrackers();
		this.lastStatement = statement;
	}

	public void setTrackedResult(ResultSet set) {
		this.clearTrackers();
		this.lastResult = set;
	}

	@Override
	public void close() throws SQLException {
		if(closed) return;
		closed = true;
		LOGGER.trace("Closed ({})", debugId);

		this.clearTrackers();

		connection.close();
	}

	private void checkClosed() {
		if(closed) throw new IllegalStateException("Already closed");
	}

	@FunctionalInterface
	public interface StatementPreparer {
		void prepare(PreparedStatement smt) throws SQLException;
	}
}