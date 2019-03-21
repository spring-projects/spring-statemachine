/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.support.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class which can be used to represent a tree based data structure.
 * This tree utility is not very generic and needs to be used as it's meant to
 * be.
 *
 * @author Janne Valkealahti
 *
 * @param <T> the type of node data
 */
public class Tree<T> {

	private final Node<T> root = new Node<T>(null);
	private final Map<Object, Node<T>> map = new HashMap<Object, Node<T>>();
	private final List<DataWrap<T>> notMapped = new ArrayList<DataWrap<T>>();

	public Node<T> getRoot() {
		return root;
	}

	public void add(T data, Object id, Object parent) {
		notMapped.add(new DataWrap<T>(data, id, parent));
		tryMapping();
	}

	private void tryMapping() {
		int size = notMapped.size();
		Iterator<DataWrap<T>> iter = notMapped.iterator();
		while(iter.hasNext()) {
			DataWrap<T> next = iter.next();
			if (next.parent == null) {
				Node<T> n = new Node<T>(next.data);
				map.put(next.id, n);
				root.getChildren().add(n);
				iter.remove();
			} else {
				if (map.containsKey(next.parent)) {
					Node<T> n = new Node<T>(next.data);
					Node<T> node = map.get(next.parent);
					map.put(next.id, n);
					node.getChildren().add(n);
					iter.remove();
				}
			}
		}
		if (notMapped.size() < size) {
			tryMapping();
		}
	}

	public static class Node<T> {
		private final T data;
		private final List<Node<T>> children;

		public Node(T data) {
			this(data, null);
		}

		public Node(T data, List<Node<T>> children) {
			this.data = data;
			this.children = children != null ? children : new ArrayList<Node<T>>();
		}

		public T getData() {
			return data;
		}

		public List<Node<T>> getChildren() {
			return children;
		}

	}

	private static class DataWrap<T> {
		final T data;
		final Object id;
		final Object parent;
		public DataWrap(T data, Object id, Object parent) {
			this.data = data;
			this.id = id;
			this.parent = parent;
		}
	}

}