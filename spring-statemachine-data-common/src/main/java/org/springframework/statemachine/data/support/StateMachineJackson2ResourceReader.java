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
package org.springframework.statemachine.data.support;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.repository.init.Jackson2ResourceReader;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.statemachine.data.BaseRepositoryEntity;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * A {@link ResourceReader} using Jackson to read JSON into objects.
 *
 * @author Oliver Gierke
 * @author Janne Valkealahti
 */
public class StateMachineJackson2ResourceReader implements ResourceReader {

	/** The Constant DEFAULT_TYPE_KEY. */
	private static final String DEFAULT_TYPE_KEY = "_class";

	/** The Constant DEFAULT_MAPPER. */
	private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

	static {
		DEFAULT_MAPPER.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/** The mapper. */
	private final ObjectMapper mapper;

	/** The type key. */
	private String typeKey = DEFAULT_TYPE_KEY;

	/**
	 * Creates a new {@link Jackson2ResourceReader}.
	 */
	public StateMachineJackson2ResourceReader() {
		this(DEFAULT_MAPPER);
	}

	/**
	 * Creates a new {@link Jackson2ResourceReader} using the given {@link ObjectMapper}.
	 *
	 * @param mapper the mapper
	 */
	public StateMachineJackson2ResourceReader(ObjectMapper mapper) {
		this.mapper = mapper == null ? DEFAULT_MAPPER : mapper;
	}

	/**
	 * Configures the JSON document's key to lookup the type to instantiate the object. Defaults to
	 * {@link Jackson2ResourceReader#DEFAULT_TYPE_KEY}.
	 *
	 * @param typeKey the new type key
	 */
	public void setTypeKey(String typeKey) {
		this.typeKey = typeKey;
	}

	@Override
	public Object readFrom(Resource resource, ClassLoader classLoader) throws Exception {
		InputStream stream = resource.getInputStream();
		ObjectReader objectReader = mapper.readerFor(JsonNode.class);
		JsonNode node = objectReader.readTree(stream);
		objectReader = mapper.readerFor(BaseRepositoryEntity[].class);

		if (node.isArray()) {
			List<Object> result = new ArrayList<Object>();
			BaseRepositoryEntity[] entitys = objectReader.readValue(node);
			result.addAll(Arrays.asList(entitys));
			return result;
		}

		return readSingle(node, classLoader);
	}

	/**
	 * Reads the given {@link JsonNode} into an instance of the type encoded in it using the configured type key.
	 *
	 * @param node must not be {@literal null}.
	 * @param classLoader the class loader
	 * @return the object
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Object readSingle(JsonNode node, ClassLoader classLoader) throws IOException {

		JsonNode typeNode = node.findValue(typeKey);
		String typeName = typeNode == null ? null : typeNode.asText();

		Class<?> type = ClassUtils.resolveClassName(typeName, classLoader);
		return mapper.readerFor(type).readValue(node);
	}
}
