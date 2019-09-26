/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.rest.hal.user;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Daniel Meyer
 *
 */
public class HalUserList extends HalResource<HalUserList> {

	public static HalUserList fromUserList(List<User> users) {
		HalUserList result = new HalUserList();

		List<HalResource<?>> halUsers = new ArrayList<HalResource<?>>();
		for (User user : users) {
			halUsers.add(HalUser.fromUser(user));
		}

		// embedd the user list
		result.addEmbedded("users", halUsers);

		// self link
		result.addLink("self", UriComponentsBuilder.fromPath(UserRestService.PATH).build().toUriString());

		return result;
	}

}