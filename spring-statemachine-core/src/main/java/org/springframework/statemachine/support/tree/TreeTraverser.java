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

import java.util.ArrayDeque;
import java.util.Iterator;

import org.springframework.util.Assert;

public abstract class TreeTraverser<T> {

	/**
	 * Returns the children of the specified node. Must not contain null.
	 * 
	 * @param root the node
	 * @return child iterables
	 */
	public abstract Iterable<T> children(T root);

	public final Iterable<T> postOrderTraversal(final T root) {
		Assert.notNull(root);
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return postOrderIterator(root);
			}
		};
	}

	Iterator<T> postOrderIterator(T root) {
		return new PostOrderIterator(root);
	}

	private static final class PostOrderNode<T> {
		final T root;
		final Iterator<T> childIterator;

		PostOrderNode(T root, Iterator<T> childIterator) {
			Assert.notNull(root);
			Assert.notNull(childIterator);
			this.root = root;
			this.childIterator = childIterator;
		}
	}

	private final class PostOrderIterator extends AbstractIterator<T> {
		private final ArrayDeque<PostOrderNode<T>> stack;

		PostOrderIterator(T root) {
			this.stack = new ArrayDeque<PostOrderNode<T>>();
			stack.addLast(expand(root));
		}

		@Override
		protected T computeNext() {
			while (!stack.isEmpty()) {
				PostOrderNode<T> top = stack.getLast();
				if (top.childIterator.hasNext()) {
					T child = top.childIterator.next();
					stack.addLast(expand(child));
				} else {
					stack.removeLast();
					return top.root;
				}
			}
			return endOfData();
		}

		private PostOrderNode<T> expand(T t) {
			return new PostOrderNode<T>(t, children(t).iterator());
		}
	}

}