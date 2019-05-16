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
package org.springframework.statemachine.support;

import java.util.List;

/**
 * Base implementation for all composite items.
 *
 * @author Janne Valkealahti
 *
 * @param <T> the type of the item
 */
public class AbstractCompositeItems<T> {

	/** List of ordered composite items */
	private OrderedCompositeItem<T> items;

	/**
	 * Constructs instance with an empty item list.
	 */
	public AbstractCompositeItems() {
		items = new OrderedCompositeItem<T>();
	}

	/**
	 * Sets the list of items. This clears
	 * all existing items.
	 *
	 * @param items the new items
	 */
	public void setItems(List<? extends T> items) {
		this.items.setItems(items);
	}

	/**
	 * Register a new item.
	 *
	 * @param item the item
	 */
	public void register(T item) {
		items.add(item);
	}

	/**
	 * Unregister a item.
	 *
	 * @param item the item
	 */
	public void unregister(T item) {
		items.remove(item);
	}

	/**
	 * Gets the items.
	 *
	 * @return the items
	 */
	public OrderedCompositeItem<T> getItems() {
		return items;
	}
}
