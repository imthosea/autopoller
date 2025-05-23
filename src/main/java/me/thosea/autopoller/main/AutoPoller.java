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
package me.thosea.autopoller.main;

import lombok.extern.log4j.Log4j2;
import me.thosea.autopoller.command.CommandHandler;
import me.thosea.autopoller.config.AutopollerConfig;
import me.thosea.autopoller.data.ApplicationLogs;
import me.thosea.autopoller.data.TrackedPolls;
import me.thosea.autopoller.data.UserDelays;
import me.thosea.autopoller.listener.ButtonListener;
import me.thosea.autopoller.listener.CommandListener;
import me.thosea.autopoller.listener.PollEndListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

@Log4j2
public final class AutoPoller {
	public final String version;
	public final AutopollerConfig config;
	public final JDA jda;
	public final Guild guild;

	private static AutoPoller instance;

	public AutoPoller(String version, AutopollerConfig config, JDA jda, Guild guild) {
		if(instance != null) {
			throw new IllegalStateException("Cannot make multiple AutoPollers");
		}
		instance = this;
		this.version = version;
		this.config = config;
		this.jda = jda;
		this.guild = guild;
		this.init();
	}

	private void init() {
		LOGGER.info("Running AutoPoller v{}", version);

		UserDelays.init();
		ApplicationLogs.init();
		TrackedPolls.init();
		scheduleSqlCleanup();

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

		Timer timer = new Timer("SQL cleanup", true);
		long delayPeriodMs = config.cooldownSeconds * 1000 + 1;
		long pollPeriodMs = config.pollLengthHours * 60 * 60 * 1000 + 1;
		// since polls have to be at least an hour long,
		// the period will always be > 1h
		long periodMs = Math.max(delayPeriodMs, pollPeriodMs);

		timer.schedule(task, 0, periodMs);
		LOGGER.info("SQL cleanup period: {} ms", periodMs);
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