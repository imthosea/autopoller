/*
 * Copyright 2026 Thosea (https://github.com/imthosea)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.thosea.autopoller.main;

import me.thosea.autopoller.command.CommandHandler;
import me.thosea.autopoller.config.AutopollerConfig;
import me.thosea.autopoller.data.ApplicationLogs;
import me.thosea.autopoller.data.TrackedPolls;
import me.thosea.autopoller.data.UserDelays;
import me.thosea.autopoller.listener.ButtonListener;
import me.thosea.autopoller.listener.CommandListener;
import me.thosea.autopoller.listener.PollEndListener;
import me.thosea.autopoller.win.PollResults;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public final class AutoPoller {
	private static final Logger LOGGER = LogManager.getLogger(AutoPoller.class);

	public final String version;
	public final AutopollerConfig config;
	public final JDA jda;
	public final Guild guild;
	public final PollResults pollResults;

	private static AutoPoller instance;

	private final Timer timer;

	public AutoPoller(String version, AutopollerConfig config, JDA jda, Guild guild) {
		if(instance != null) {
			throw new IllegalStateException("Cannot make multiple AutoPollers");
		}
		instance = this;
		this.version = version;
		this.config = config;
		this.jda = jda;
		this.guild = guild;
		this.timer = new Timer("AutoPoller", /*isDaemon*/ true);
		this.pollResults = new PollResults();
		this.init();
	}

	private void init() {
		LOGGER.info("Running AutoPoller v{}", version);

		UserDelays.init();
		ApplicationLogs.init();
		TrackedPolls.init();
		scheduleSqlCleanup();
		scheduleEarlyWin();

		jda.addEventListener(
				new CommandListener(),
				new ButtonListener(),
				new PollEndListener()
		);
		if(!CommandHandler.IS_ERROR) {
			jda.updateCommands().addCommands(CommandHandler.DATA_ARRAY).queue();
		}
	}

	private void scheduleSqlCleanup() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				LOGGER.info("Running SQL cleanup");

				try {
					UserDelays.cleanup();
				} catch(Exception e) {
					LOGGER.error("Error doing user delay cleanup", e);
				}
				try {
					TrackedPolls.cleanup();
				} catch(Exception e) {
					LOGGER.error("Error doing tracked polls cleanup", e);
				}
			}
		};

		long delayPeriodMs = config.cooldownSeconds * 1000 + 1;
		long pollPeriodMs = config.pollLengthHours * 60 * 60 * 1000 + 1;
		// since polls have to be at least an hour long,
		// the period will always be > 1h
		long periodMs = Math.max(delayPeriodMs, pollPeriodMs);

		this.timer.schedule(task, 0, periodMs);
		LOGGER.info("SQL cleanup period: {} ms", periodMs);
	}

	private void scheduleEarlyWin() {
		if(config.pollEarlyWinCheckInterval <= 0 || config.pollEarlyWinCount <= 0) {
			LOGGER.info("Poll early win disabled");
			return;
		}

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				LOGGER.info("Checking for early poll wins");
				Thread.startVirtualThread(() -> {
					try {
						pollResults.checkEarlyWins();
					} catch(Exception e) {
						LOGGER.error("Error checking for early poll wins", e);
					}
				});
			}
		};

		long periodMs = config.pollEarlyWinCheckInterval * 60 * 1000;
		this.timer.schedule(task, 0, periodMs);
		LOGGER.info("Early win check cleanup: {} ms", periodMs);
	}

	public boolean isOurGuild(Guild guild) {
		return guild != null && guild.getIdLong() == config.guildId;
	}

	public boolean isOurGuild(long guildId) {
		return guildId == config.guildId;
	}

	@NotNull
	public <T> T getOrThrow(String name, Supplier<T> getter) {
		T thing = getter.get();
		if(thing == null)
			throw new IllegalStateException("Missing " + name);
		return thing;
	}

	public static AutoPoller instance() {
		if(instance == null)
			throw new IllegalStateException("Not loaded yet");
		return instance;
	}
}