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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

@Command
public class CdPlayerCommands {

	@Autowired
	private CdPlayer cdPlayer;

	@Autowired
	private Library library;

	@Command(command = "cd lcd", description = "Prints CD player lcd info")
	public String lcd() {
		return cdPlayer.getLdcStatus();
	}

	@Command(command = "cd library", description = "List user CD library")
	public String library() {
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

	@Command(command = "cd load", description = "Load CD into player")
	public String load(@Option(longNames = {"", "index"}) int index) {
		StringBuilder buf = new StringBuilder();
		try {
			Cd cd = library.getCollection().get(index);
			cdPlayer.load(cd);
			buf.append("Loading cd " + cd);
		} catch (Exception e) {
			buf.append("Cd with index " + index + " not found, check library");
		}
		return buf.toString();
	}

	@Command(command = "cd play", description = "Press player play button")
	public void play() {
		cdPlayer.play();
	}

	@Command(command = "cd stop", description = "Press player stop button")
	public void stop() {
		cdPlayer.stop();
	}

	@Command(command = "cd pause", description = "Press player pause button")
	public void pause() {
		cdPlayer.pause();
	}

	@Command(command = "cd eject", description = "Press player eject button")
	public void eject() {
		cdPlayer.eject();
	}

	@Command(command = "cd forward", description = "Press player forward button")
	public void forward() {
		cdPlayer.forward();
	}

	@Command(command = "cd back", description = "Press player back button")
	public void back() {
		cdPlayer.back();
	}

}
