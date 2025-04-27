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

package me.thosea.autopoller.listener;

import lombok.extern.log4j.Log4j2;
import me.thosea.autopoller.config.AutopollerConfig;
import me.thosea.autopoller.config.AutopollerMessages;
import me.thosea.autopoller.data.TrackedPolls;
import me.thosea.autopoller.data.TrackedPolls.TrackedPollEntry;
import me.thosea.autopoller.main.AutoPoller;
import me.thosea.autopoller.util.RequestUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.entities.messages.MessagePoll.Answer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Collections;
import java.util.List;

@Log4j2
public final class PollEndListener extends ListenerAdapter {
	private static final AutoPoller BOT = AutoPoller.instance();
	private static final AutopollerConfig CONFIG = BOT.config;
	private static final AutopollerMessages MSG = CONFIG.messages;

	private final Emoji acknowledgeReaction;
	private final Role winRole = BOT.getOrThrow("win role", () -> {
		return BOT.guild.getRoleById(BOT.config.pollWinRoleId);
	});

	public PollEndListener() {
		this.acknowledgeReaction = Emoji.fromUnicode(MSG.endPollAcknowledgeReactionEmoji);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(!BOT.isOurGuild(event.getGuild())) return;

		Message endMsg = event.getMessage();
		if(!endMsg.getAuthor().equals(BOT.jda.getSelfUser())) return;
		if(endMsg.getType() != MessageType.POLL_RESULT) return;

		MessageReference ref = endMsg.getMessageReference();
		if(ref == null) return;

		MessageChannel channel = endMsg.getChannel();
		long channelId = channel.getIdLong();
		long pollMessageId = ref.getMessageIdLong();

		LOGGER.trace("Lookup channel {} / message {}", channelId, pollMessageId);

		Thread.startVirtualThread(() -> {
			TrackedPollEntry entry = TrackedPolls.getEntry(channel.getIdLong(), pollMessageId);
			LOGGER.trace("Poll entry: {}", entry);
			if(entry == null) return;

			Message pollMsg = RequestUtils.safeComplete(() -> {
				return channel.retrieveMessageById(pollMessageId);
			});
			if(pollMsg == null) {
				LOGGER.trace("But " + /*nobody came*/ "there was no message");
				return; // message deleted probably
			}

			LOGGER.trace("Poll message found: {}", pollMsg::getJumpUrl);

			try {
				handlePollResult(entry, pollMsg);
				endMsg.addReaction(acknowledgeReaction).complete();
			} catch(Exception e) {
				LOGGER.warn("Error handling poll end", e);
			}
		});
	}

	private void handlePollResult(TrackedPollEntry entry, Message pollMsg) {
		MessagePoll poll = pollMsg.getPoll();
		if(poll == null)
			throw new IllegalStateException("Poll is missing?");

		List<Answer> answers = poll.getAnswers();
		if(answers.size() != 2)
			throw new IllegalStateException("Poll does not have exactly 2 answers");

		TextChannel ticket = BOT.guild.getTextChannelById(entry.ticketChannelId());
		ThreadChannel thread = BOT.guild.getThreadChannelById(entry.threadChannelId());
		if(ticket == null || thread == null) {
			throw new IllegalStateException("Missing ticket or thread channel (" +
					"Ticket ID: " + entry.ticketChannelId() +
					"Thread ID: " + entry.threadChannelId()
					+ ")");
		}

		if(isWin(answers.getFirst().getVotes(), answers.get(1).getVotes())) {
			Member member = RequestUtils.safeComplete(() -> {
				return BOT.guild.retrieveMemberById(entry.applicantId());
			});
			if(member == null) {
				throw new IllegalStateException("@" + entry.applicantUsername() + " (" + entry.applicantId() + ") not found");
			}

			BOT.guild.addRoleToMember(member, winRole).complete();
			sendMessage(MSG.endPollWin, entry, ticket, thread);
		} else {
			sendMessage(MSG.endPollLose, entry, ticket, thread);
		}
	}

	private void sendMessage(String msg, TrackedPollEntry entry, TextChannel ticket, ThreadChannel thread) {
		msg = msg.formatted(entry.applicantId(), winRole.getAsMention());

		List<MentionType> mentions = Collections.singletonList(MentionType.USER);
		ticket.sendMessage(msg).setAllowedMentions(mentions).queue();
		thread.sendMessage(msg).setAllowedMentions(mentions).queue();
	}

	private boolean isWin(int yesVotes, int noVotes) {
		LOGGER.trace("{} to {} votes", yesVotes, noVotes);
		if(yesVotes == 0) return false;

		float yesPercent = ((float) yesVotes / (yesVotes + noVotes)) * 100;
		LOGGER.trace("Yes percent: {} / Required: {}", yesPercent, CONFIG.pollWinPercent);
		return yesPercent >= CONFIG.pollWinPercent;
	}
}