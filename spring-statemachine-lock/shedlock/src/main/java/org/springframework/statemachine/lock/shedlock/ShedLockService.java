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
package org.springframework.statemachine.lock.shedlock;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.lock.LockService;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * This implementation uses ShedLock (https://github.com/jesty/ShedLock) in order to handle distributed locks on state machines.
 * ShedLock offers many providers like Mongo, JDBC database, Redis, Hazelcast, ZooKeeper, CosmosDB or others.
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ShedLockService<S, E> implements LockService<S, E> {

    private final static Logger log = LoggerFactory.getLogger(ShedLockService.class);

    private final Map<String, SimpleLock> locks;
    private final LockProvider lockProvider;

    public ShedLockService(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
        this.locks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean lock(StateMachine<S, E> stateMachine, int lockAtMostUntil) {
        String id = stateMachine.getId();
        LockConfiguration lockConfiguration = new LockConfiguration(id, Instant.now().plus(Duration.ofSeconds(lockAtMostUntil)));
        Optional<SimpleLock> simpleLock = lockProvider.lock(lockConfiguration);
        if (simpleLock.isPresent()) {
            log.trace("Lock acquired for state machine with id {}, simpleLock: {}", id, simpleLock);
            locks.put(id, simpleLock.get());
            return true;
        } else {
            log.warn("Cannot acquire lock for state machine with id {}", id);
            return false;
        }
    }

    @Override
    public void unLock(StateMachine<S, E> stateMachine) {
        String id = stateMachine.getId();
        SimpleLock simpleLock = locks.remove(id);
        if (simpleLock != null) {
            log.trace("Starting unlock on {}", id);
            simpleLock.unlock();
        }
    }

}
