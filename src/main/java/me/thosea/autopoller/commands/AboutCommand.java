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
package me.thosea.autopoller.commands;

import me.thosea.autopoller.command.CommandHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public final class AboutCommand extends CommandHandler {
	@Override
	protected SlashCommandData makeCommandData() {
		return Commands.slash("about", MSG.descAbout);
	}

	@Override
	public void handle(Member member, User user, SlashCommandInteraction event) {
		event.reply(MSG.about)
				.setEphemeral(true)
				.setAllowedMentions(List.of())
				.queue();
	}
}