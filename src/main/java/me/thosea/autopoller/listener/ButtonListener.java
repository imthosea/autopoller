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
import me.thosea.autopoller.button.ButtonHandler;
import me.thosea.autopoller.main.AutoPoller;
import me.thosea.autopoller.util.ErrorReporter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Log4j2
public final class ButtonListener extends ListenerAdapter {
	private static final AutoPoller BOT = AutoPoller.instance();

	public ButtonListener() {
		// noinspection ResultOfMethodCallIgnored - handle error quickly
		ButtonHandler.BUTTONS.isEmpty();
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if(!BOT.isOurGuild(event.getGuild())) return;
		if(ButtonHandler.IS_ERROR) {
			event.reply("A configuration error occurred and button handlers failed to load. Please contact an admin.")
					.setEphemeral(true)
					.queue();
			LOGGER.warn("Rejected @{}'s button click because there was an error", event.getUser().getName());
			return;
		}

		Member member = event.getMember();
		if(member == null) return;
		User user = event.getUser();

		String id = event.getComponentId();
		ButtonHandler handler = ButtonHandler.BUTTONS.get(id);
		LOGGER.debug("@{} clicked button {}, handler: {}", user.getName(), id, handler);

		if(handler != null) {
			try {
				handler.handle(member, user, event);
			} catch(Exception e) {
				ErrorReporter.error(user, event, "button " + id, e);
			}
		}
	}
}