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
package me.thosea.autopoller.buttons;

import lombok.extern.log4j.Log4j2;
import me.thosea.autopoller.button.DeferredButtonHandler;
import me.thosea.autopoller.data.ApplicationLogs;
import me.thosea.autopoller.data.UserDelays;
import me.thosea.autopoller.util.FormatUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel.AutoArchiveDuration;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.messages.MessagePollData;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("DataFlowIssue")
@Log4j2
public final class MakeAppButton extends DeferredButtonHandler {
	private final Category category = BOT.getOrThrow("tickets category", () -> {
		return BOT.guild.getCategoryById(BOT.config.ticketsCategoryId);
	});
	private final TextChannel pollChannel = BOT.getOrThrow("poll channel", () -> {
		return BOT.guild.getTextChannelById(BOT.config.pollChannelId);
	});

	private final Role ticketViewerRoleId = BOT.getOrThrow("ticket viewer role", () -> {
		return BOT.guild.getRoleById(BOT.config.ticketViewerRoleId);
	});

	private final Emoji emojiOpt1 = Emoji.fromUnicode(MSG.makeAppPollOpt1Emoji);
	private final Emoji emojiOpt2 = Emoji.fromUnicode(MSG.makeAppPollOpt2Emoji);

	private final Color embedColor = Color.decode(MSG.makeAppTicketEmbedColor);

	public MakeAppButton() {
		// maybe i should've just used kotlin...
		super(/*isEphemeral=*/ true, /*useVirtualThread=*/true);
	}

	@Override
	protected boolean preDefer(Member member, ButtonInteraction event) {
		return true;
	}

	@Override
	protected void handleDeferred(Member member, InteractionHook hook) {
		if(!UserDelays.addDelay(member.getIdLong())) {
			LOGGER.trace("On cooldown: {}", () -> member.getUser().getName());
			hook.editOriginal(MSG.makeAppCooldown).queue();
			return;
		}

		int count = ApplicationLogs.getAppCount(member.getIdLong()) + 1;
		String countStr = FormatUtils.withEnglishSuffix(count);

		LOGGER.info("@{} is making their {} application", member.getUser().getName(), countStr);

		String userMention = member.getAsMention();
		TextChannel ticket = makeTicket(member, userMention, count, countStr);
		Message pollMessage = sendPoll(member, userMention, countStr, ticket.getAsMention());

		String url = pollMessage.getJumpUrl();
		LOGGER.debug(
				"\nUser ID: {}\nTicket Channel ID: {}\nPoll Message URL: {}",
				member.getIdLong(), ticket.getIdLong(), url);
		ApplicationLogs.addLog(member.getIdLong(), ticket.getIdLong(), url);

		hook.editOriginal(MSG.makeAppSuccess.formatted(ticket.getAsMention())).queue();
	}

	private static final List<Permission> ALLOW_PERMISSIONS = List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY);
	private static final List<Permission> DENY_PERMISSIONS = List.of(Permission.VIEW_CHANNEL);

	private TextChannel makeTicket(Member member, String userMention,
	                               int count, String countStr) {
		TextChannel ticket = BOT.guild.createTextChannel(
						member.getUser().getName() + "-application-" + count,
						category)
				.addMemberPermissionOverride(member.getIdLong(), ALLOW_PERMISSIONS, null)
				.addPermissionOverride(BOT.guild.getBotRole(), ALLOW_PERMISSIONS, null)
				.addPermissionOverride(ticketViewerRoleId, ALLOW_PERMISSIONS, null)
				.addPermissionOverride(BOT.guild.getPublicRole(), null, DENY_PERMISSIONS)
				.complete();

		ticket.sendMessageEmbeds(new EmbedBuilder()
						.setDescription(MSG.makeAppTicketEmbedDesc.formatted(userMention, countStr))
						.setColor(embedColor)
						.build())
				.queue();
		ticket.sendMessage(userMention).flatMap(Message::delete).queue();
		return ticket;
	}

	private Message sendPoll(Member member, String userMention,
	                         String countStr, String ticketMention) {
		String text = MSG.makeAppPollMessage.formatted(userMention, countStr, ticketMention);
		Message msg = pollChannel.sendMessage(text)
				.setPoll(MessagePollData.builder(MSG.makeAppPoll)
						.addAnswer(MSG.makeAppPollOpt1, emojiOpt1)
						.addAnswer(MSG.makeAppPollOpt2, emojiOpt2)
						.setDuration(CONFIG.pollLengthHours, TimeUnit.HOURS)
						.build())
				.setAllowedMentions(List.of(MentionType.ROLE, MentionType.HERE, MentionType.EVERYONE))
				.complete();

		String threadName = MSG.makeAppPollThreadName.formatted(member.getUser().getName(), countStr);
		msg.createThreadChannel(threadName)
				.setAutoArchiveDuration(AutoArchiveDuration.TIME_3_DAYS)
				.queue();
		return msg;
	}
}