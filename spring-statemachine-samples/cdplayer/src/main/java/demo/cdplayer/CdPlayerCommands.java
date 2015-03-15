package demo.cdplayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class CdPlayerCommands implements CommandMarker {

	@Autowired
	private CdPlayer cdPlayer;

	@Autowired
	private Library library;

	@CliCommand(value = "cd lcd", help = "Prints CD player lcd info")
	public String lcd() {
		return cdPlayer.getLdcStatus();
	}

	@CliCommand(value = "cd library", help = "List user CD library")
	public String library() {
		StringBuilder buf = new StringBuilder();
		int index = 0;
		for (Cd cd : library.getCollection()) {
			buf.append(index++ + ": " + cd.getName() + "\n");
		}
		return buf.toString();
	}

	@CliCommand(value = "cd load", help = "Load CD into player")
	public String load(@CliOption(key = {"", "index"}) int index) {
		StringBuilder buf = new StringBuilder();
		try {
			cdPlayer.load(library.getCollection().get(index));
			buf.append("Loading cd " + index);
		} catch (Exception e) {
			buf.append("Cd with index " + index + " not found");
		}
		return buf.toString();
	}

	@CliCommand(value = "cd play", help = "Press player play button")
	public void play() {
		cdPlayer.play();
	}

	@CliCommand(value = "cd stop", help = "Press player stop button")
	public void stop() {
		cdPlayer.stop();
	}

	@CliCommand(value = "cd pause", help = "Press player pause button")
	public void pause() {
		cdPlayer.pause();
	}

	@CliCommand(value = "cd eject", help = "Press player eject button")
	public void eject() {
		cdPlayer.eject();
	}

	@CliCommand(value = "cd forward", help = "Press player forward button")
	public void forward() {
		cdPlayer.forward();
	}

	@CliCommand(value = "cd back", help = "Press player back button")
	public void back() {
		cdPlayer.back();
	}

}
