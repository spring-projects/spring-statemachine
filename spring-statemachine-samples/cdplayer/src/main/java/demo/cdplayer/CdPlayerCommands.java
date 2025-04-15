/*
 * Copyright 2015-2025 the original author or authors.
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
package demo.cdplayer;

import java.text.SimpleDateFormat;
import java.util.Date;

import demo.AbstractStateMachineCommands;
import demo.BasicCommand;
import demo.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdPlayerCommands extends AbstractStateMachineCommands<Application.States, Application.Events> {

	@Autowired
	private CdPlayer cdPlayer;

	@Autowired
	private Library library;

	@Bean
	public Command lcd() {
		return new BasicCommand("lcd", "Prints CD player lcd info") {
			@Override
			public String execute(String[] args) {
				return cdPlayer.getLcdStatus();
			}
		};
	}

	@Bean
	public Command list() {
		return new BasicCommand("list", "List user CD library") {
			@Override
			public String execute(String[] args) {
				SimpleDateFormat format = new SimpleDateFormat("mm:ss");
				StringBuilder buf = new StringBuilder();
				int i1 = 0;
				for (Cd cd : library.getCollection()) {
					buf.append(i1++ + ": " + cd.getName() + "\n");
					int i2 = 0;
					for (Track track : cd.getTracks()) {
						buf.append("  " + i2++ + ": " + track.getName() + "  " + format.format(new Date(track.getLength()*1000)) + "\n");
					}
				}
				return buf.toString();
			}
		};
	}

	@Bean
	public Command load() {
		return new BasicCommand("load [index]", "Load CD [index] into player") {
			@Override
			public String execute(String[] args) {
				StringBuilder buf = new StringBuilder();
				int index = Integer.parseInt(args[0]);
				try {
					Cd cd = library.getCollection().get(index);
					cdPlayer.load(cd);
					buf.append("Loading cd " + cd);
				} catch (Exception e) {
					buf.append("Cd with index " + index + " not found, check library");
				}
				return buf.toString();
			}
		};
	}

	@Bean
	public Command play() {
		return new BasicCommand("play", "Press player play button") {
			@Override
			public String execute(String[] args) {
				cdPlayer.play();
				return "";
			}
		};
	}

	@Bean
	public Command stop() {
		return new BasicCommand("stop", "Press player stop button") {
			@Override
			public String execute(String[] args) {
				cdPlayer.stop();
				return "";
			}
		};
	}

	@Bean
	public Command pause() {
		return new BasicCommand("pause", "Press player pause button") {
			@Override
			public String execute(String[] args) {
				cdPlayer.pause();
				return "";
			}
		};
	}

	@Bean
	public Command eject() {
		return new BasicCommand("eject", "Press player eject button") {
			@Override
			public String execute(String[] args) {
				cdPlayer.eject();
				return "";
			}
		};
	}

	@Bean
	public Command forward() {
		return new BasicCommand("forward", "Press player forward button") {
			@Override
			public String execute(String[] args) {
				cdPlayer.forward();
				return "";
			}
		};
	}

	@Bean
	public Command back() {
		return new BasicCommand("back", "Press player back button") {
			@Override
			public String execute(String[] args) {
				cdPlayer.back();
				return "";
			}
		};
	}

}
