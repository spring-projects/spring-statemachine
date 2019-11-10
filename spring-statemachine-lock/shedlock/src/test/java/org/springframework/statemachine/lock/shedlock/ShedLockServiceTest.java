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
package org.springframework.statemachine.lock.shedlock;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.lock.LockService;
import org.springframework.statemachine.lock.LockStateMachineGuard;
import org.springframework.statemachine.lock.LockStateMachineListener;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

public class ShedLockServiceTest {

    private AnnotationConfigApplicationContext context;
    private RedisConnection connection;

    @BeforeEach
    public void setup() {
        context = buildContext();
        context.register(ShedLockConfig.class, Config1.class);
        context.refresh();
        connection = context.getBean(RedisConnectionFactory.class).getConnection();
        connection.flushAll();
    }

    @AfterEach
    public void clean() {
        if (context != null) {
            context.close();
        }
    }

    protected AnnotationConfigApplicationContext buildContext() {
        return new AnnotationConfigApplicationContext();
    }

    @Test
    public void testLockOnLockedMachine() {
        LockService<String, String> lockService = context.getBean(LockService.class);
        StateMachine<String, String> stateMachine = getStateMachine();

        boolean lock = lockService.lock(stateMachine, 120);
        assertThat(lock).isTrue();

        StateMachineEventResult<String, String> lockResult = stateMachine.sendEvent(Mono.just(buildE1Event())).blockLast();
        assertThat(stateMachine.getState().getId()).isEqualTo("S1");
        assertThat(connection.exists("job-lock:test:testId".getBytes())).isTrue();
    }

    @Test
    public void testLock() {
        StateMachine<String, String> stateMachine = getStateMachine();
        assertThat(stateMachine.getState().getId()).isEqualTo("S1");
        StateMachineEventResult<String, String> lockResult = stateMachine.sendEvent(Mono.just(buildE1Event())).blockLast();
        assertThat(lockResult).isNotNull();
        assertThat(lockResult.getResultType()).isEqualTo(StateMachineEventResult.ResultType.ACCEPTED);
        assertThat(stateMachine.getState().getId()).isEqualTo("S2");
        assertThat(connection.exists("job-lock:test:testId".getBytes())).isFalse();
    }

    @Test
    public void testLockInSameState() {
        StateMachine<String, String> stateMachine = getStateMachine();
        assertThat(stateMachine.getState().getId()).isEqualTo("S1");
        StateMachineEventResult<String, String> lockResult = stateMachine.sendEvent(Mono.just(buildE1Event())).blockLast();

        assertThat(lockResult).isNotNull();
        assertThat(stateMachine.getState().getId()).isEqualTo("S2");
        assertThat(connection.exists("job-lock:test:testId".getBytes())).isFalse();

        StateMachineEventResult<String, String> lockResultAfter = stateMachine.sendEvent(Mono.just(buildE1Event())).blockLast();
        assertThat(lockResultAfter).isNotNull();
        assertThat(stateMachine.getState().getId()).isEqualTo("S2");
        assertThat(lockResult.getResultType()).isEqualTo(StateMachineEventResult.ResultType.ACCEPTED);
        assertThat(connection.exists("job-lock:test:testId".getBytes())).isFalse();


    }

    private StateMachine<String, String> getStateMachine() {
        StateMachineFactory<String, String> factory = context.getBean(StateMachineFactory.class);
        StateMachine<String, String> stateMachine = factory.getStateMachine("testId");
        LockService<String, String> lockService = context.getBean(LockService.class);
        return stateMachine;
    }

    private Message<String> buildE1Event() {
        return MessageBuilder
                .withPayload("E1")
                .build();
    }

    @Configuration
    protected static class ShedLockConfig {

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return new JedisConnectionFactory();
        }

        @Bean
        public LockService lockService(RedisConnectionFactory connectionFactory) {
            LockProvider lockProvider = new RedisLockProvider(connectionFactory, "test");
            return new ShedLockService(lockProvider);
        }

    }

    @Configuration
    @EnableStateMachineFactory
    static class Config1 extends StateMachineConfigurerAdapter<String, String> {

        @Autowired
        private LockService<String, String> lockService;

        @Override
        public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
            config
                    .withConfiguration()
                    .listener(new LockStateMachineListener<>(lockService))
                    .autoStartup(true);
        }

        @Override
        public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
            states
                    .withStates()
                    .initial("S1")
                    .state("S2")
                    .state("S3");
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
            transitions
                    .withExternal()
                    .guard(new LockStateMachineGuard<>(lockService, 120))
                    .source("S1").target("S2")
                    .event("E1")
                    .action(stateContext -> System.out.println("Action on " + stateContext.getStateMachine().getId()))
                    .and()
                    .withExternal()
                    .source("S2").target("S3")
                    .event("E2")
                    .and()
                    .withExternal()
                    .source("S3").target("S1")
                    .event("E3");
        }

    }

}
