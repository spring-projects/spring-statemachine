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
		Track cd1track1 = new Track("Bohemian Rhapsody", 5 * 60 + 56);
		Track cd1track2 = new Track("Another One Bites the Dust", 3 * 60 + 36);
		Cd cd1 = new Cd("Greatest Hits", new Track[] { cd1track1, cd1track2 });
		Track cd2track1 = new Track("A Kind of Magic", 4 * 60 + 22);
		Track cd2track2 = new Track("Under Pressure", 4 * 60 + 8);
		Cd cd2 = new Cd("Greatest Hits II", new Track[] { cd2track1, cd2track2 });
		return new Library(new Cd[] { cd1, cd2 });
	}

}
