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
package demo.persist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.recipes.persist.PersistStateMachineHandler;
import org.springframework.statemachine.recipes.persist.PersistStateMachineHandler.PersistStateChangeListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import demo.persist.Application.Order;

public class Persist {

	private final PersistStateMachineHandler handler;

//tag::snippetA[]
	@Autowired
	private JdbcTemplate jdbcTemplate;
//end::snippetA[]

	private final PersistStateChangeListener listener = new LocalPersistStateChangeListener();

	public Persist(PersistStateMachineHandler handler) {
		this.handler = handler;
		this.handler.addPersistStateChangeListener(listener);
	}

	public String listDbEntries() {
		List<Order> orders = jdbcTemplate.query(
		        "select id, state from orders",
		        new RowMapper<Order>() {
		            public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
		            	return new Order(rs.getInt("id"), rs.getString("state"));
		            }
		        });
		StringBuilder buf = new StringBuilder();
		for (Order order : orders) {
			buf.append(order);
			buf.append("\n");
		}
		return buf.toString();
	}

//tag::snippetB[]
	public void change(int order, String event) {
		Order o = jdbcTemplate.queryForObject("select id, state from orders where id = ?", new Object[] { order },
				new RowMapper<Order>() {
					public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new Order(rs.getInt("id"), rs.getString("state"));
					}
				});
		handler.handleEventWithState(MessageBuilder.withPayload(event).setHeader("order", order).build(), o.state);
	}

	//end::snippetB[]

//tag::snippetC[]
	private class LocalPersistStateChangeListener implements PersistStateChangeListener {

		@Override
		public void onPersist(State<String, String> state, Message<String> message,
				Transition<String, String> transition, StateMachine<String, String> stateMachine) {
			if (message != null && message.getHeaders().containsKey("order")) {
				Integer order = message.getHeaders().get("order", Integer.class);
				jdbcTemplate.update("update orders set state = ? where id = ?", state.getId(), order);
			}
		}
	}
//end::snippetC[]

}
