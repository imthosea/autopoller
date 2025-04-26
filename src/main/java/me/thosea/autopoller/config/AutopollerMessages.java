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
package me.thosea.autopoller.config;

import java.util.Properties;

import static me.thosea.autopoller.config.AutopollerConfig.str;

public final class AutopollerMessages {
	public final String noPermission;
	public final String error;

	public final String application;
	public final String applicationButton;
	public final String applicationMsgSent;

	public final String about;

	public final String makeAppCooldown;
	public final String makeAppTicketEmbedDesc;
	public final String makeAppTicketEmbedColor;
	public final String makeAppPollMessage;
	public final String makeAppPoll;
	public final String makeAppPollOpt1;
	public final String makeAppPollOpt1Emoji;
	public final String makeAppPollOpt2;
	public final String makeAppPollOpt2Emoji;
	public final String makeAppPollThreadName;
	public final String makeAppSuccess;

	public final String removeCooldownNoCooldown;
	public final String removeCooldownSuccess;

	public final String archiveCmnNotTicket;
	public final String archiveCmnChannelMoved;

	public final String archiveAlreadyArchived;
	public final String archiveSuccess;

	public final String unarchiveNotArchived;
	public final String unarchiveSuccess;

	public final String listAppsTitle;
	public final String listAppsTitleEmpty;
	public final String listAppsDescEmpty;
	public final String listAppsDescLine;
	public final String listAppsColor;

	public final String descSendApplicationMessage;
	public final String descAbout;
	public final String descRemoveCooldown;
	public final String descRemoveCooldownArg;
	public final String descArchiveChannel;
	public final String descUnarchiveChannel;
	public final String descListApplications;
	public final String descListApplicationsArg;

	public AutopollerMessages(Properties prop) {
		this.noPermission = str(prop, "msg.no_permission");
		this.error = str(prop, "msg.error");

		this.application = str(prop, "msg.application");
		this.applicationButton = str(prop, "msg.application_button");
		this.applicationMsgSent = str(prop, "msg.application_message_sent");

		this.about = str(prop, "msg.about");

		this.makeAppCooldown = str(prop, "msg.make_app.cooldown");
		this.makeAppTicketEmbedDesc = str(prop, "msg.make_app.ticket_embed.desc");
		this.makeAppTicketEmbedColor = str(prop, "msg.make_app.ticket_embed.color");
		this.makeAppPollMessage = str(prop, "msg.make_app.poll_message");
		this.makeAppPoll = str(prop, "msg.make_app.poll");
		this.makeAppPollOpt1 = str(prop, "msg.make_app.poll.opt1");
		this.makeAppPollOpt1Emoji = str(prop, "msg.make_app.poll.opt1_emoji");
		this.makeAppPollOpt2 = str(prop, "msg.make_app.poll.opt2");
		this.makeAppPollOpt2Emoji = str(prop, "msg.make_app.poll.opt2_emoji");
		this.makeAppPollThreadName = str(prop, "msg.make_app.poll_thread_name");
		this.makeAppSuccess = str(prop, "msg.make_app.success");

		this.removeCooldownNoCooldown = str(prop, "msg.remove_cooldown.no_cooldown");
		this.removeCooldownSuccess = str(prop, "msg.remove_cooldown.success");

		this.archiveCmnNotTicket = str(prop, "msg.archive_common.not_ticket");
		this.archiveCmnChannelMoved = str(prop, "msg.archive_common.channel_moved");

		this.archiveAlreadyArchived = str(prop, "msg.archive_channel.already_archived");
		this.archiveSuccess = str(prop, "msg.archive_channel.success");

		this.unarchiveNotArchived = str(prop, "msg.unarchive_channel.not_archived");
		this.unarchiveSuccess = str(prop, "msg.unarchive_channel.success");

		this.listAppsTitleEmpty = str(prop, "msg.list_applications.embed.title_empty");
		this.listAppsTitle = str(prop, "msg.list_applications.embed.title");
		this.listAppsDescEmpty = str(prop, "msg.list_applications.embed.desc_empty");
		this.listAppsDescLine = str(prop, "msg.list_applications.embed.desc_line");
		this.listAppsColor = str(prop, "msg.list_applications.embed.color");

		this.descSendApplicationMessage = str(prop, "msg.desc.sendapplicationmessage");
		this.descAbout = str(prop, "msg.desc.about");
		this.descRemoveCooldown = str(prop, "msg.desc.removecooldown");
		this.descRemoveCooldownArg = str(prop, "msg.desc.removecooldown.arg");
		this.descArchiveChannel = str(prop, "msg.desc.archivechannel");
		this.descUnarchiveChannel = str(prop, "msg.desc.unarchivechannel");
		this.descListApplications = str(prop, "msg.desc.listapplications");
		this.descListApplicationsArg = str(prop, "msg.desc.listapplications.arg");
	}
}