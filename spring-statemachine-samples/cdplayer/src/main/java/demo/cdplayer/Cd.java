package demo.cdplayer;

public class Cd {

	private final String name;
	private final Track[] tracks;

	public Cd(String name, Track[] tracks) {
		this.name = name;
		this.tracks = tracks;
	}

	public String getName() {
		return name;
	}

	public Track[] getTracks() {
		return tracks;
	}

}