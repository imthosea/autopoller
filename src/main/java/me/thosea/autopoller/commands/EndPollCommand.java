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
import me.thosea.autopoller.data.TrackedPolls;
import me.thosea.autopoller.util.FormatUtils;
import me.thosea.autopoller.util.FormatUtils.ParseResult;
import me.thosea.autopoller.util.RequestUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class EndPollCommand extends DeferredCommandHandler {
	public EndPollCommand() {
		super(/*isEphemeral=*/true, /*useVirtualThread=*/false);
	}

	@Override
	protected SlashCommandData makeCommandData() {
		return Commands.slash("endpoll", MSG.descEndPoll)
				.addOption(OptionType.STRING, "url", MSG.descEndPollArg1, true)
				.addOption(OptionType.BOOLEAN, "ignore_result", MSG.descEndPollArg2, true);
	}

	@Override
	protected boolean preDefer(Member member, User user, SlashCommandInteraction event) {
		if(!member.hasPermission(Permission.MANAGE_THREADS)) {
			event.reply(MSG.noPermission).setEphemeral(true).queue();
			return false;
		}

		return true;
	}

	@SuppressWarnings("DataFlowIssue")
	@Override
	protected void handleDeferred(Member member, User user, CommandInteractionPayload cmd, InteractionHook hook) {
		String url = cmd.getOption("url", OptionMapping::getAsString);
		boolean ignoreResult = cmd.getOption("ignore_result", OptionMapping::getAsBoolean);

		ParseResult parse = FormatUtils.parseMessageUrl(url);
		if(parse == null) {
			hook.editOriginal(MSG.endPollBadUrl).queue();
			return;
		}

		TextChannel channel = BOT.guild.getTextChannelById(parse.channelId());
		if(channel == null) {
			hook.editOriginal(MSG.endPollNoResult).queue();
			return;
		}

		Thread.startVirtualThread(() -> {
			handle(hook, channel, parse, ignoreResult);
		});
	}

	private void handle(InteractionHook hook, TextChannel channel, ParseResult parse, boolean ignoreResult) {
		Message msg = RequestUtils.safeComplete(() -> {
			return channel.retrieveMessageById(parse.messageId());
		});
		if(msg == null) {
			hook.editOriginal(MSG.endPollNoResult).queue();
			return;
		} else if(!msg.getAuthor().equals(BOT.jda.getSelfUser())) {
			hook.editOriginal(MSG.endPollNotMine).queue();
			return;
		} else if(msg.getPoll() == null) {
			hook.editOriginal(MSG.endPollNotPoll).queue();
			return;
		} else if(msg.getPoll().isExpired()) {
			hook.editOriginal(MSG.endPollAlreadyEnded).queue();
			return;
		}

		if(ignoreResult) {
			TrackedPolls.dropEntry(channel.getIdLong(), msg.getIdLong());
		}

		try {
			msg.endPoll().complete();
			hook.editOriginal(MSG.endPollSuccess).queue();
		} catch(RuntimeException ignored) {
			hook.editOriginal(MSG.endPollFailed).queue();
		}
	}
}