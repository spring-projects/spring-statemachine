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
package org.springframework.statemachine.state;

import java.util.Iterator;

import org.springframework.statemachine.listener.AbstractCompositeListener;

public class CompositePseudoStateListener<S, E> extends AbstractCompositeListener<PseudoStateListener<S, E>> implements
		PseudoStateListener<S, E> {

	@Override
	public void onContext(PseudoStateContext<S, E> context) {
		for (Iterator<PseudoStateListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			PseudoStateListener<S, E> listener = iterator.next();			
			listener.onContext(context);
		}		
	}

}
