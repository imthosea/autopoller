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

package me.thosea.autopoller.win;

import me.thosea.autopoller.config.AutopollerConfig;
import me.thosea.autopoller.config.AutopollerMessages;
import me.thosea.autopoller.data.TrackedPolls;
import me.thosea.autopoller.data.TrackedPolls.TrackedPollEntry;
import me.thosea.autopoller.main.AutoPoller;
import me.thosea.autopoller.util.RequestUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.entities.messages.MessagePoll.Answer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class PollResults {
	private static final Logger LOGGER = LogManager.getLogger(PollResults.class);
	private static final AutoPoller BOT = AutoPoller.instance();
	private static final AutopollerConfig CONFIG = BOT.config;
	private static final AutopollerMessages MSG = CONFIG.messages;

	private final Role winRole = BOT.getOrThrow("win role", () -> {
		return BOT.guild.getRoleById(BOT.config.pollWinRoleId);
	});

	private final Emoji acknowledgeReaction = Emoji.fromUnicode(MSG.endPollAcknowledgeReactionEmoji);

	public void winPoll(TrackedPollEntry entry) {
		LOGGER.info("Poll for {} has won", entry.applicantUsername());
		Member member = RequestUtils.safeComplete(() -> {
			return BOT.guild.retrieveMemberById(entry.applicantId());
		});
		if(member == null) {
			throw new IllegalStateException("@" + entry.applicantUsername() + " (" + entry.applicantId() + ") not found");
		}
		BOT.guild.addRoleToMember(member, winRole).complete();

		GuildChannel thread = BOT.guild.getGuildChannelById(entry.threadChannelId());
		if(thread != null) {
			thread.delete().complete();
		}
	}

	public void checkEarlyWins() {
		for(TrackedPollEntry entry : TrackedPolls.getEntries()) {
			TextChannel channel = BOT.guild.getTextChannelById(entry.pollChannelId());
			if(channel == null) continue;

			Message msg = RequestUtils.safeComplete(() -> {
				return channel.retrieveMessageById(entry.pollMessageId());
			});
			List<Answer> answers = getPollAnswers(msg);
			if(answers == null) return;

			int yesVotes = answers.getFirst().getVotes();
			LOGGER.trace("Current yes votes: {} / Early win required: {}", yesVotes, BOT.config.pollEarlyWinCount);
			if(yesVotes >= BOT.config.pollEarlyWinCount) {
				TrackedPolls.dropEntry(entry);
				winPoll(entry);
				sendResultMessage(MSG.endPollEarlyWin, entry);
				msg.endPoll().complete();
				msg.addReaction(acknowledgeReaction).complete();
			}
		}
	}

	public void onPollEnd(TrackedPollEntry entry, Message msg) {
		List<Answer> answers = getPollAnswers(msg);
		if(answers == null) return;

		int yesVotes = answers.getFirst().getVotes();
		int noVotes = answers.get(1).getVotes();

		LOGGER.trace("{} to {} votes", yesVotes, noVotes);

		float yesPercent = yesVotes != 0
				? ((float) yesVotes / (yesVotes + noVotes)) * 100
				: 0f;
		LOGGER.trace("Yes percent: {} / Required: {}", yesPercent, CONFIG.pollWinPercent);

		if(yesPercent >= CONFIG.pollWinPercent) {
			BOT.pollResults.winPoll(entry);
			sendResultMessage(MSG.endPollWin, entry);
		} else {
			sendResultMessage(MSG.endPollLose, entry);
		}
		msg.addReaction(acknowledgeReaction).complete();
		TrackedPolls.dropEntry(entry);
	}

	@Nullable
	private List<Answer> getPollAnswers(Message msg) {
		if(msg == null) {
			return null;
		}
		MessagePoll poll = msg.getPoll();
		if(poll == null) {
			return null;
		} else if(poll.getAnswers().size() != 2) {
			return null;
		} else {
			return poll.getAnswers();
		}
	}

	private void sendResultMessage(String msg, TrackedPollEntry entry) {
		TextChannel ticket = BOT.guild.getTextChannelById(entry.ticketChannelId());
		if(ticket == null) return;

		msg = msg.formatted(entry.applicantId(), winRole.getAsMention());
		ticket.sendMessage(msg)
				.setAllowedMentions(Collections.singletonList(MentionType.USER))
				.queue();
	}
}