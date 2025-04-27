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

import me.thosea.autopoller.main.AutoPoller;
import org.jetbrains.annotations.Nullable;

public final class FormatUtils {
	private FormatUtils() {}

	private static final AutoPoller BOT = AutoPoller.instance();

	public static String withEnglishSuffix(int num) {
		if(num >= 11 && num <= 13)
			return num + "th";
		return num + switch(num % 10) {
			case 1 -> "st";
			case 2 -> "nd";
			case 3 -> "rd";
			default -> "th";
		};
	}

	@Nullable
	public static ParseResult parseMessageUrl(String url) {
		if(!url.startsWith("https://discord.com/channels/")) return null;

		String[] split = url.split("/");
		if(split.length < 7) return null;

		try {
			long guild = Long.parseLong(split[4]);
			long channel = Long.parseLong(split[5]);
			long msg = Long.parseLong(split[6]);
			if(BOT.isOurGuild(guild)) {
				return new ParseResult(channel, msg);
			}
		} catch(NumberFormatException ignored) {}
		return null;
	}

	public record ParseResult(long channelId, long messageId) {}
}