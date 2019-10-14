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
package org.springframework.statemachine.lock.redisson;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.lock.LockService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This implementation uses Redisson (https://github.com/redisson/redisson) in order to handle distributed locks on state machines.
 * More info on https://carlosbecker.com/posts/distributed-locks-redis/ and https://github.com/redisson/redisson/wiki/8.-Distributed-locks-and-synchronizers
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class RedissonLockService<S, E> implements LockService<S, E> {

    private final static Logger log = LoggerFactory.getLogger(RedissonLockService.class);

    protected final static String LOCK_PREFIX = "lock:";

    private RedissonClient redissonClient;
    private final Map<String, RLock> locks;

    public RedissonLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.locks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean lock(StateMachine<S, E> stateMachine, int lockAtMostUntil) {
        String id = buildLockId(stateMachine);
        RLock lock = this.redissonClient.getLock(id);
        boolean result = false;
        try {
            log.trace("Lock acquired for state machine with id {}, Rlock: {}", id, lock);
            result = lock.tryLock(0, lockAtMostUntil, TimeUnit.SECONDS);
            this.locks.put(id, lock);
        } catch (InterruptedException e) {
            log.warn("Cannot acquire lock for state machine with id {}", id);
        }
        return result;
    }

    @Override
    public void unLock(StateMachine<S, E> stateMachine) {
        String id = buildLockId(stateMachine);
        RLock lock = locks.remove(id);
        if (lock != null) {
            log.trace("Starting unlock on {}", id);
            lock.unlock();
        }
    }

    private String buildLockId(StateMachine<S, E> stateMachine) {
        return LOCK_PREFIX + stateMachine.getId();
    }

}
