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

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.internal.entities.MemberImpl;

import java.util.Collection;

public final class RoleUtils {
	private RoleUtils() {}

	public static boolean hasRole(Member member, Role role) {
		if(role == null) return false;

		// member#getRoles() makes and sorts a new List<Role> every call,
		// so avoid it if possible
		Collection<Role> roles = member instanceof MemberImpl impl ? impl.getRoleSet() : member.getRoles();
		return roles.contains(role);
	}
}