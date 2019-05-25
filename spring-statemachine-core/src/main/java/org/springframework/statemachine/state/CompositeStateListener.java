/*
 * Copyright 2016-2019 the original author or authors.
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.listener.AbstractCompositeListener;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Composite state listener.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class CompositeStateListener<S, E> extends AbstractCompositeListener<StateListener<S, E>>
		implements StateListener<S, E> {

	@Override
	public void onEntry(StateContext<S, E> context) {
		for (Iterator<StateListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			iterator.next().onEntry(context);
		}
	}

	@Override
	public void onExit(StateContext<S, E> context) {
		for (Iterator<StateListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			iterator.next().onExit(context);
		}
	}

	@Override
	public void onComplete(StateContext<S, E> context) {
		for (Iterator<StateListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			iterator.next().onComplete(context);
		}
	}

	@Override
	public Mono<Void> doOnComplete(StateContext<S, E> context) {
		return Mono.defer(() -> {
			Iterator<StateListener<S, E>> iterator = getListeners().reverse();
			Iterable<StateListener<S, E>> iterable = () -> iterator;
			Stream<StateListener<S, E>> stream = StreamSupport.stream(iterable.spliterator(), false);
			return Flux.fromStream(stream)
				.flatMap(listener -> listener.doOnComplete(context))
				.then();
		});
	}
}
