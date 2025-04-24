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
package me.thosea.autopoller.command;

import lombok.extern.log4j.Log4j2;
import me.thosea.autopoller.commands.AboutCommand;
import me.thosea.autopoller.commands.ArchiveChannelCommand;
import me.thosea.autopoller.commands.ListApplicationsCommand;
import me.thosea.autopoller.commands.RemoveCooldownCommand;
import me.thosea.autopoller.commands.SendApplicationMessageCommand;
import me.thosea.autopoller.commands.UnarchiveChannelCommand;
import me.thosea.autopoller.config.AutopollerConfig;
import me.thosea.autopoller.config.AutopollerMessages;
import me.thosea.autopoller.main.AutoPoller;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public abstract class CommandHandler {
	protected abstract SlashCommandData makeCommandData();
	public abstract void handle(Member member, SlashCommandInteraction event);

	public static final Map<String, CommandHandler> COMMANDS;
	public static final SlashCommandData[] DATA_ARRAY;
	public static final boolean IS_ERROR;

	protected static final AutoPoller BOT = AutoPoller.instance();
	protected static final AutopollerConfig CONFIG = BOT.config;
	protected static final AutopollerMessages MSG = CONFIG.messages;

	static {
		Map<String, CommandHandler> commands;
		SlashCommandData[] dataArray;
		boolean error;

		try {
			@SuppressWarnings("StaticInitializerReferencesSubClass")
			List<CommandHandler> handlers = List.of(
					new SendApplicationMessageCommand(),
					new AboutCommand(),
					new RemoveCooldownCommand(),
					new ArchiveChannelCommand(),
					new UnarchiveChannelCommand(),
					new ListApplicationsCommand()
			);

			dataArray = new SlashCommandData[handlers.size()];
			commands = new HashMap<>(handlers.size());

			for(int i = 0; i < handlers.size(); i++) {
				CommandHandler handler = handlers.get(i);
				SlashCommandData data = handler.makeCommandData();

				dataArray[i] = data;
				commands.put(data.getName(), handler);
			}
			error = false;
		} catch(Exception e) {
			LOGGER.error("Failed to load a command handler, most likely due to a configuration error.", e);
			LOGGER.error("All commands will be disabled.");
			commands = null;
			dataArray = null;
			error = true;
		}

		COMMANDS = commands;
		DATA_ARRAY = dataArray;
		IS_ERROR = error;
	}

	@Override
	public String toString() {
		return "CommandHandler[class=" + getClass().getSimpleName() + "]";
	}
}