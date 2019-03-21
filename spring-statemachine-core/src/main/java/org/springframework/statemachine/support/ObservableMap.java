/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;

/**
 * Utility class which wraps {@link Map} and notifies
 * {@link MapChangeListener} of changes for individual
 * change operations.
 *
 * @author Janne Valkealahti
 *
 * @param <K> the type of key
 * @param <V> the type of value
 */
public class ObservableMap<K, V> implements Map<K, V> {

	private volatile Map<K, V> delegate;
	private volatile MapChangeListener<K, V> listener;

	/**
	 * Instantiates a new observable map.
	 */
	public ObservableMap() {
		// default constructor needed for kryo, thus
		// we create delegate here, listener not needed.
		delegate = new ConcurrentHashMap<K, V>();
	}

	/**
	 * Instantiates a new observable map.
	 *
	 * @param map the delegating map
	 * @param listener the map change listener
	 */
	public ObservableMap(Map<K, V> map, MapChangeListener<K, V> listener) {
		Assert.notNull(map, "Delegating map must be set");
		Assert.notNull(listener, "Listener must be set");
		this.delegate = map;
		this.listener = listener;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return delegate.get(key);
	}

	@Override
	public V put(K key, V value) {
		V put = delegate.put(key, value);
		if (listener != null) {
			if (put == null) {
				listener.added(key, value);
			} else if (value != null && !value.equals(put)) {
				listener.changed(key, value);
			}
		}
		return put;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		V remove = delegate.remove(key);
		if (listener != null && remove != null) {
			listener.removed((K)key, remove);
		}
		return remove;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		delegate.putAll(m);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public Set<K> keySet() {
		return delegate.keySet();
	}

	@Override
	public Collection<V> values() {
		return delegate.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	/**
	 * Gets the delegating map instance.
	 *
	 * @return the delegate
	 */
	public Map<K, V> getDelegate() {
		return delegate;
	}

	/**
	 * Sets the delegate.
	 *
	 * @param delegate the delegate
	 */
	public void setDelegate(Map<K, V> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Sets the map change listener.
	 *
	 * @param listener the listener
	 */
	public void setListener(MapChangeListener<K, V> listener) {
		this.listener = listener;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ObservableMap<?, ?> other = (ObservableMap<?, ?>) obj;
		if (delegate == null) {
			if (other.delegate != null) {
				return false;
			}
		} else if (!delegate.equals(other.delegate)) {
			return false;
		}
		return true;
	}

	/**
	 * The listener interface for receiving map change events.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 */
	public interface MapChangeListener<K, V> {

		/**
		 * Called when new entry is added.
		 *
		 * @param key the key
		 * @param value the value
		 */
		void added(K key, V value);

		/**
		 * Called when entry has been changed.
		 *
		 * @param key the key
		 * @param value the value
		 */
		void changed(K key, V value);

		/**
		 * Called when entry has been removed.
		 *
		 * @param key the key
		 * @param value the value
		 */
		void removed(K key, V value);

	}

}