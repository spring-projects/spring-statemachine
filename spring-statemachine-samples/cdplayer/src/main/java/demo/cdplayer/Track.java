package demo.cdplayer;

public class Track {

	private final String name;
	private final long length;

	public Track(String name, long length) {
		this.name = name;
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public long getLength() {
		return length;
	}

	@Override
	public String toString() {
		return name;
	}

}