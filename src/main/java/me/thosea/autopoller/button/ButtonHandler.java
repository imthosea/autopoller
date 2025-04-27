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

import lombok.extern.log4j.Log4j2;
import me.thosea.autopoller.buttons.MakeAppButton;
import me.thosea.autopoller.config.AutopollerConfig;
import me.thosea.autopoller.config.AutopollerMessages;
import me.thosea.autopoller.main.AutoPoller;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.util.Map;

@Log4j2
public abstract class ButtonHandler {
	public abstract void handle(Member member, User user, ButtonInteraction event);

	protected static final AutoPoller BOT = AutoPoller.instance();
	protected static final AutopollerConfig CONFIG = BOT.config;
	protected static final AutopollerMessages MSG = CONFIG.messages;

	public static final Map<String, ButtonHandler> BUTTONS;
	public static final boolean IS_ERROR;

	static {
		Map<String, ButtonHandler> buttons;
		boolean error;
		try {
			// noinspection StaticInitializerReferencesSubClass
			buttons = Map.of(ButtonIds.MAKE_APP, new MakeAppButton());
			error = false;
		} catch(Exception e) {
			LOGGER.error("Failed to load a button handler, most likely due to a configuration error.", e);
			LOGGER.error("All buttons will be disabled.");
			buttons = null;
			error = true;
		}
		BUTTONS = buttons;
		IS_ERROR = error;
	}

	@Override
	public String toString() {
		return "ButtonHandler[class=" + getClass().getSimpleName() + "]";
	}
}