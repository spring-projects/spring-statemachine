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

import org.junit.Test;
import org.springframework.statemachine.support.tree.Tree.Node;

public class TreeTests {

	@Test
	public void testTree1() {
		Tree<String> tree = new Tree<String>();
		tree.add("S", "S", null);
		tree.add("S1", "S1", "S");
		tree.add("S2", "S2", "S");
		tree.add("S11", "S11", "S1");
		tree.add("S12", "S12", "S1");
		tree.add("S13", "S13", "S1");
		tree.add("S21", "S21", "S2");

		TreeTraverser<Node<String>> traverser = new TreeTraverser<Node<String>>() {
		    @Override
		    public Iterable<Node<String>> children(Node<String> root) {
		        return root.getChildren();
		    }
		};

		for (Node<String> node : traverser.postOrderTraversal(tree.getRoot())) {
			System.out.println(node.getData());
		}
	}

	@Test
	public void testTree2() {
		Tree<String> tree = new Tree<String>();
		tree.add("S2", "S2", "S");
		tree.add("S13", "S13", "S1");
		tree.add("S11", "S11", "S1");
		tree.add("S", "S", null);
		tree.add("S12", "S12", "S1");
		tree.add("S21", "S21", "S2");
		tree.add("S1", "S1", "S");

		TreeTraverser<Node<String>> traverser = new TreeTraverser<Node<String>>() {
		    @Override
		    public Iterable<Node<String>> children(Node<String> root) {
		        return root.getChildren();
		    }
		};

		for (Node<String> node : traverser.postOrderTraversal(tree.getRoot())) {
			System.out.println(node.getData());
		}
	}

	@Test
	public void testTree3() {
		Tree<String> tree = new Tree<String>();
		tree.add("S1", "S1", null);
		tree.add("S2", "S2", null);

		TreeTraverser<Node<String>> traverser = new TreeTraverser<Node<String>>() {
		    @Override
		    public Iterable<Node<String>> children(Node<String> root) {
		        return root.getChildren();
		    }
		};

		for (Node<String> node : traverser.postOrderTraversal(tree.getRoot())) {
			System.out.println(node.getData());
		}
	}

}
