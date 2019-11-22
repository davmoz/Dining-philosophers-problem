package dm222is.src;

/*
 * File:	Chopstick.java
 * Course: 	Operating Systems
 * Code: 	1DV512
 * Author: 	Suejb Memeti (modified by Kostiantyn Kucher)
 * Date: 	November 2019
 */

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Chopstick {
	private final int id;
	private Lock myLock = new ReentrantLock();

	public Chopstick(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Lock getLock() {
		return myLock;
	}
}
