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

package me.thosea.autopoller.listener;

import me.thosea.autopoller.data.TrackedPolls;
import me.thosea.autopoller.data.TrackedPolls.TrackedPollEntry;
import me.thosea.autopoller.main.AutoPoller;
import me.thosea.autopoller.util.RequestUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PollEndListener extends ListenerAdapter {
	private static final Logger LOGGER = LogManager.getLogger(PollEndListener.class);
	private static final AutoPoller BOT = AutoPoller.instance();

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
				BOT.pollResults.onPollEnd(entry, pollMsg);
			} catch(Exception e) {
				LOGGER.warn("Error handling poll end", e);
			}
		});
	}
}