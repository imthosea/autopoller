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
package me.thosea.autopoller.button;

import me.thosea.autopoller.util.ErrorReporter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

// Uh ... is this too many layers of abstraction?
// (yes or no, do NOT point me to those 4000 page java design books that make builder patterns or singletons or literally everything look like sin)
public abstract class DeferredButtonHandler extends ButtonHandler {
	protected final boolean isEphemeral;
	protected final boolean useVirtualThread;

	protected DeferredButtonHandler(boolean isEphemeral, boolean useVirtualThread) {
		this.isEphemeral = isEphemeral;
		this.useVirtualThread = useVirtualThread;
	}

	@Override
	public final void handle(Member member, User user, ButtonInteraction event) {
		if(!preDefer(member, user, event))
			return;

		String buttonId = event.getComponentId();
		event.deferReply().setEphemeral(this.isEphemeral).queue(hook -> {
			if(this.useVirtualThread) {
				Thread.startVirtualThread(() -> {
					callDeferredHandler(member, user, buttonId, hook);
				});
			} else {
				callDeferredHandler(member, user, buttonId, hook);
			}
		});
	}

	private void callDeferredHandler(Member member, User user,
	                                 String buttonId, InteractionHook hook) {
		try {
			this.handleDeferred(member, user, hook);
		} catch(Exception e) {
			ErrorReporter.deferredError(user, hook, "button " + buttonId, e);
		}
	}

	@Override
	public String toString() {
		return "DeferredButtonHandler[class=" + getClass().getSimpleName() + "]";
	}

	protected abstract boolean preDefer(Member member, User user, ButtonInteraction event);
	protected abstract void handleDeferred(Member member, User user, InteractionHook hook);
}