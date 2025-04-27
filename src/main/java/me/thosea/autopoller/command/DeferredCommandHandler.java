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
package me.thosea.autopoller.command;

import me.thosea.autopoller.util.ErrorReporter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public abstract class DeferredCommandHandler extends CommandHandler {
	protected final boolean isEphemeral;
	protected final boolean useVirtualThread;

	protected DeferredCommandHandler(boolean isEphemeral, boolean useVirtualThread) {
		this.isEphemeral = isEphemeral;
		this.useVirtualThread = useVirtualThread;
	}

	@Override
	public final void handle(Member member, User user, SlashCommandInteraction event) {
		if(!preDefer(member, user, event))
			return;

		event.deferReply().setEphemeral(isEphemeral).queue(hook -> {
			String commandName = event.getName();
			if(useVirtualThread) {
				Thread.startVirtualThread(() -> {
					callDeferredHandler(member, user, event, commandName, hook);
				});
			} else {
				callDeferredHandler(member, user, event, commandName, hook);
			}
		});
	}

	private void callDeferredHandler(Member member, User user,
	                                 CommandInteractionPayload cmd, String commandName,
	                                 InteractionHook hook) {
		try {
			this.handleDeferred(member, user, cmd, hook);
		} catch(Exception e) {
			ErrorReporter.deferredError(user, hook, "command " + commandName, e);
		}
	}

	@Override
	public String toString() {
		return "DeferredCommandHandler[class=" + getClass().getSimpleName() + "]";
	}

	protected abstract boolean preDefer(Member member, User user, SlashCommandInteraction event);
	protected abstract void handleDeferred(Member member, User user, CommandInteractionPayload cmd, InteractionHook hook);
}