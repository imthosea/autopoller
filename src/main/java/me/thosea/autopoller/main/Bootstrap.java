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
import me.thosea.autopoller.config.AutopollerConfig;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log4j2
public final class Bootstrap {
	public static void main(String[] args) {
		String version;
		AutopollerConfig config;

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try(InputStream stream = loader.getResourceAsStream("autopoller_version.txt")) {
			Objects.requireNonNull(stream, "Version file not in jar");
			version = new String(stream.readAllBytes()).trim();
		} catch(IOException e) {
			throw new RuntimeException("Failed to read version file", e);
		}

		try {
			config = AutopollerConfig.load(LOGGER);
		} catch(Exception e) {
			throw new RuntimeException("Failed to read config", e);
		}

		Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
		// RestActionImpl.setDefaultFailure(error -> {}); // uncomment to hide exceptions

		JDABuilder.createDefault(config.token)
				.enableIntents(GatewayIntent.GUILD_MEMBERS)
				.setStatus(OnlineStatus.ONLINE)
				.addEventListeners(new ListenerAdapter() {
					@Override
					public void onGuildReady(GuildReadyEvent event) {
						Guild guild = event.getGuild();
						if(guild.getIdLong() != config.guildId) {
							LOGGER.info("Wrong guild {} ({})", guild.getName(), guild.getId());
							return;
						}

						LOGGER.info("Guild {} ready, loading", guild.getName());
						event.getJDA().removeEventListener(this);
						new AutoPoller(version, config, event.getJDA(), guild);
					}
				})
				.build();

		LOGGER.info("Waiting for guild with ID {}", config.guildId);
	}
}