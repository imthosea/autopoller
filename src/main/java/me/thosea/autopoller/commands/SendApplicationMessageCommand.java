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
import me.thosea.autopoller.button.ButtonIds;
import me.thosea.autopoller.command.CommandHandler;
import me.thosea.autopoller.util.ErrorReporter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Log4j2
public final class SendApplicationMessageCommand extends CommandHandler {
	@Override
	protected SlashCommandData makeCommandData() {
		return Commands.slash(
				"sendapplicationmessage", // NO INTELLIJ, THAT'S DEFINITELY A WORD, SHUSH
				MSG.descSendApplicationMessage);
	}

	@Override
	public void handle(Member member, SlashCommandInteraction event) {
		if(!member.hasPermission(Permission.MANAGE_SERVER)) {
			event.reply(MSG.noPermission).setEphemeral(true).queue();
			return;
		}

		MessageChannelUnion channel = event.getChannel();
		LOGGER.info("@{} sent application message to #{}", member.getUser().getName(), channel.getName());

		event.deferReply().setEphemeral(true).queue(hook -> {
			handle(member, hook, channel);
		});
	}

	private void handle(Member member, InteractionHook hook, MessageChannelUnion channel) {
		channel.sendMessage(MSG.application)
				.addActionRow(Button.primary(ButtonIds.MAKE_APP, MSG.applicationButton))
				.queue(
						_ -> hook.editOriginal(MSG.applicationMsgSent).queue(),
						e -> ErrorReporter.deferredError(member, hook, "application message sending", e)
				);
	}
}