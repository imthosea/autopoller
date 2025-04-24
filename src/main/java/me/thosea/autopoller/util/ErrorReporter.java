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
package me.thosea.autopoller.util;

import lombok.extern.log4j.Log4j2;
import me.thosea.autopoller.main.AutoPoller;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

@Log4j2(topic = /*"Oh Crap"*/"AutoPollerErrors")
public final class ErrorReporter {
	private ErrorReporter() {}

	private static final String MSG = AutoPoller.instance().config.messages.error;

	public static void error(Member member, IReplyCallback event,
	                         String task, Throwable e) {
		LOGGER.error("Error processing {} for @{}", task, member.getUser().getName(), e);
		event.reply(MSG).setEphemeral(true).queue();
	}

	public static void deferredError(Member member, InteractionHook hook,
	                                 String task, Throwable e) {
		LOGGER.error("Error processing deferred {} for @{}", task, member.getUser().getName(), e);
		hook.editOriginal(MSG).queue();
	}
}