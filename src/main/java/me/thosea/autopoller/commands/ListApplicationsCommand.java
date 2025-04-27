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
package me.thosea.autopoller.commands;

import lombok.extern.log4j.Log4j2;
import me.thosea.autopoller.command.DeferredCommandHandler;
import me.thosea.autopoller.data.ApplicationLogs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.Color;

@Log4j2
public class ListApplicationsCommand extends DeferredCommandHandler {
	private final Color embedColor = Color.decode(MSG.listAppsColor);

	public ListApplicationsCommand() {
		super(/*isEphemeral=*/ true, /*useVirtualThread=*/false);
	}

	@Override
	protected SlashCommandData makeCommandData() {
		return Commands.slash("listapplications", MSG.descListApplications)
				.addOption(OptionType.USER, "target", MSG.descListApplicationsArg, true);
	}

	@Override
	protected boolean preDefer(Member member, User user, SlashCommandInteraction event) {
		if(!member.hasPermission(Permission.MANAGE_THREADS)) {
			LOGGER.trace("@{}: no permission", user.getName());
			event.reply(MSG.noPermission).setEphemeral(true).queue();
			return false;
		}
		return true;
	}

	@Override
	protected void handleDeferred(Member member, User user, CommandInteractionPayload cmd, InteractionHook hook) {
		User target = cmd.getOption("target", OptionMapping::getAsUser);
		assert target != null; // arg is required

		LOGGER.debug("@{} is listing for @{}", user.getName(), target.getName());

		EmbedBuilder embed = new EmbedBuilder();
		StringBuilder builder = new StringBuilder();

		ApplicationLogs.listApplications(target.getIdLong(), set -> {
			if(!set.next()) {
				embed.setTitle(MSG.listAppsTitleEmpty.formatted(target.getName()));
				builder.append(MSG.listAppsDescEmpty);
				return;
			}

			boolean isFirst = true;
			int rows = 0;
			do {
				if(isFirst) {
					isFirst = false;
				} else {
					builder.append("\n");
				}
				rows++;

				String line = MSG.listAppsDescLine.formatted(
						set.getString("date"),
						set.getLong("ticket_channel"),
						set.getString("poll_message_url")
				);
				builder.append(line);
			} while(set.next());

			embed.setTitle("Applications for @" + target.getName() + " (Total: " + rows + ")");
		});

		embed.setColor(embedColor);
		embed.setDescription(builder.toString());
		hook.editOriginalEmbeds(embed.build()).queue();
	}
}