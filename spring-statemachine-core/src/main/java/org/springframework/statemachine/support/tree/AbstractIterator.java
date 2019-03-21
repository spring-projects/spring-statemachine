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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.springframework.util.Assert;

public abstract class AbstractIterator<T> implements Iterator<T> {

	private State state = State.NOT_READY;

	private enum State {
		READY, NOT_READY, DONE, FAILED,
	}

	private T next;

	protected abstract T computeNext();

	protected final T endOfData() {
		state = State.DONE;
		return null;
	}

	@Override
	public final boolean hasNext() {
		Assert.state(state != State.FAILED);
		switch (state) {
		case DONE:
			return false;
		case READY:
			return true;
		default:
		}
		return tryToComputeNext();
	}

	private boolean tryToComputeNext() {
		state = State.FAILED;
		next = computeNext();
		if (state != State.DONE) {
			state = State.READY;
			return true;
		}
		return false;
	}

	@Override
	public final T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		state = State.NOT_READY;
		T result = next;
		next = null;
		return result;
	}

	public final T peek() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		return next;
	}

	public final void remove() {
		throw new UnsupportedOperationException("remove");
	}

}