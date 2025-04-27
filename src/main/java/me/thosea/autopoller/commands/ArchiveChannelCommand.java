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

import me.thosea.autopoller.command.DeferredCommandHandler;
import me.thosea.autopoller.util.ChannelUtils;
import me.thosea.autopoller.util.ErrorReporter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public final class ArchiveChannelCommand extends DeferredCommandHandler {
	private final Category targetCategory = BOT.getOrThrow("archived tickets category", () -> {
		return BOT.guild.getCategoryById(CONFIG.archivedTicketsCategoryId);
	});

	public ArchiveChannelCommand() {
		super(/*isEphemeral=*/ false, /*useVirtualThread=*/false);
	}

	@Override
	protected SlashCommandData makeCommandData() {
		return Commands.slash("archivechannel", MSG.descArchiveChannel);
	}

	@Override
	protected boolean preDefer(Member member, User user, SlashCommandInteraction event) {
		if(!member.hasPermission(Permission.MANAGE_THREADS)) {
			event.reply(MSG.noPermission).setEphemeral(true).queue();
			return false;
		}

		long category = ChannelUtils.getCategoryId(event.getChannel());
		if(category == CONFIG.archivedTicketsCategoryId) {
			event.reply(MSG.archiveAlreadyArchived).setEphemeral(true).queue();
			return false;
		} else if(category != CONFIG.ticketsCategoryId) {
			event.reply(MSG.archiveCmnNotTicket).setEphemeral(true).queue();
			return false;
		}
		return true;
	}

	@Override
	protected void handleDeferred(Member member, User user, CommandInteractionPayload cmd, InteractionHook hook) {
		var channel = (GuildMessageChannel) hook.getInteraction().getGuildChannel();
		if(ChannelUtils.getCategoryId(channel) != CONFIG.ticketsCategoryId) {
			hook.editOriginal(MSG.archiveCmnChannelMoved).queue();
			return;
		}

		((ICategorizableChannel) channel).getManager().setParent(targetCategory).queue(
				_ -> {
					hook.editOriginal(MSG.archiveSuccess.formatted(user.getAsMention()))
							.setAllowedMentions(List.of())
							.queue();
				},
				e -> ErrorReporter.deferredError(user, hook, "channel archive", e)
		);
	}
}