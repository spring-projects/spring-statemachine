package demo.cdplayer;

import java.util.Arrays;
import java.util.List;

public class Library {

	private final List<Cd> collection;

	public Library(Cd[] collection) {
		this.collection = Arrays.asList(collection);
	}

	public List<Cd> getCollection() {
		return collection;
	}

	public static Library buildSampleLibrary() {
		Track cd1track1 = new Track("Bohemian Rhapsody", 5*60+56);
		Track cd1track2 = new Track("Another One Bites the Dust", 3*60+36);
		Cd cd1 = new Cd("Greatest Hits", new Track[]{cd1track1,cd1track2});
		Track cd2track1 = new Track("A Kind of Magic", 4*60+22);
		Track cd2track2 = new Track("Under Pressure", 4*60+8);
		Cd cd2 = new Cd("Greatest Hits II", new Track[]{cd2track1,cd2track2});
		return new Library(new Cd[]{cd1,cd2});
	}

}
