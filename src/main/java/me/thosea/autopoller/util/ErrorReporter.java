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
package me.thosea.autopoller.util;

import me.thosea.autopoller.main.AutoPoller;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ErrorReporter {
	private ErrorReporter() {}

	private static final Logger LOGGER = LogManager.getLogger(/*Oh Crap*/ "AutoPollerErrors");
	private static final String MSG = AutoPoller.instance().config.messages.error;

	public static void error(User user, IReplyCallback event,
	                         String task, Throwable error) {
		LOGGER.error("Error processing {} for @{}", task, user.getName(), error);
		event.reply(MSG).setEphemeral(true).queue();
	}

	public static void deferredError(User user, InteractionHook hook,
	                                 String task, Throwable error) {
		LOGGER.error("Error processing deferred {} for @{}", task, user.getName(), error);
		hook.editOriginal(MSG).queue();
	}
}