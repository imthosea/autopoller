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
import me.thosea.autopoller.data.UserDelays;
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

import java.util.List;

@Log4j2
public final class RemoveCooldownCommand extends DeferredCommandHandler {
	public RemoveCooldownCommand() {
		super(/*isEphemeral=*/ true, /*useVirtualThread=*/true);
	}

	@Override
	protected SlashCommandData makeCommandData() {
		return Commands.slash("removecooldown", MSG.descRemoveCooldown)
				.addOption(OptionType.USER, "target", MSG.descRemoveCooldownArg, true);
	}

	@Override
	protected boolean preDefer(Member member, SlashCommandInteraction event) {
		if(!member.hasPermission(Permission.MANAGE_THREADS)) {
			LOGGER.trace("No permission: {}", () -> member.getUser().getName());
			event.reply(MSG.noPermission).setEphemeral(true).queue();
			return false;
		}
		return true;
	}

	@Override
	protected void handleDeferred(Member member, CommandInteractionPayload cmd, InteractionHook hook) {
		User target = cmd.getOption("target", OptionMapping::getAsUser);
		assert target != null; // arg is required

		if(UserDelays.removeDelay(target.getIdLong())) {
			hook.editOriginal(MSG.removeCooldownSuccess.formatted(target.getAsMention()))
					.setAllowedMentions(List.of())
					.queue();
			LOGGER.info("@{} removed @{}'s application delay", member.getUser().getName(), target.getName());
		} else {
			hook.editOriginal(MSG.removeCooldownNoCooldown.formatted(target.getAsMention()))
					.setAllowedMentions(List.of())
					.queue();
		}
	}
}