/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.data.redis;

import org.springframework.data.annotation.Id;

//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.statemachine.data.RepositoryAction;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Redis entity for actions.
 *
 * @author Janne Valkealahti
 *
 */
@RedisHash("RedisRepositoryAction")
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public class RedisRepositoryAction extends RepositoryAction {

	@Id
	private String id;

	private String name;
	private String spel;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getSpel() {
		return spel;
	}

	public void setSpel(String spel) {
		this.spel = spel;
	}
}
