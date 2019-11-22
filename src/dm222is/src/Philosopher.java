package dm222is.src;

/*
 * File:	Philosopher.java
 * Course: 	Operating Systems
 * Code: 	1DV512
 * Author: 	Suejb Memeti (modified by Kostiantyn Kucher)
 * Date: 	November 2019
 */

import java.util.Random;

public class Philosopher implements Runnable {

	public boolean DEBUG = true;

	private boolean interrupted = false;

	private int id;

	private final Chopstick leftChopstick;
	private final Chopstick rightChopstick;

	private Random randomGenerator = new Random();

	private int numberOfEatingTurns = 0;
	private int numberOfThinkingTurns = 0;
	private int numberOfHungryTurns = 0;

	private double thinkingTime = 0;
	private double eatingTime = 0;
	private double hungryTime = 0;

	public Philosopher(int id, Chopstick leftChopstick, Chopstick rightChopstick, int seed, boolean debug) {
		this.id = id;
		this.leftChopstick = leftChopstick;
		this.rightChopstick = rightChopstick;

		this.DEBUG = debug;

		randomGenerator.setSeed(id + seed);
	}

	public int getId() {
		return id;
	}

	public double getAverageThinkingTime() {
		/*
		 * If the number of turns is 0, then average is 0
		 * 
		 * Validating number of turns to avoid division by 
		 * zero / Arithmetic exceptions
		 */
		if (numberOfEatingTurns != 0) {
			return thinkingTime / numberOfThinkingTurns;
		}
		return 0;
	}

	public double getAverageEatingTime() {
		/*
		 * If the number of turns is 0, then average is 0
		 * 
		 * Validating number of turns to avoid division by 
		 * zero / Arithmetic exceptions
		 */
		if (numberOfEatingTurns != 0) {
			return eatingTime / numberOfEatingTurns;
		}
		return 0;
	}

	public double getAverageHungryTime() {
		/*
		 * If the number of turns is 0, then average is 0
		 * 
		 * Validating number of turns to avoid division by 
		 * zero / Arithmetic exceptions
		 */
		if (numberOfHungryTurns != 0) {
			return hungryTime / numberOfHungryTurns;
		}
		return 0;
	}

	public int getNumberOfThinkingTurns() {
		return numberOfThinkingTurns;
	}

	public int getNumberOfEatingTurns() {
		return numberOfEatingTurns;
	}

	public int getNumberOfHungryTurns() {
		return numberOfHungryTurns;
	}

	public double getTotalThinkingTime() {
		return thinkingTime;
	}

	public double getTotalEatingTime() {
		return eatingTime;
	}

	public double getTotalHungryTime() {
		return hungryTime;
	}

	@Override
	public void run() {

		do {
			// Thinking state
			think();

			// Hungry state, if not interrupted
			if (!interrupted) {
				printDebugMessage("Philosopher " + id + " is hungry");
				long currentTimeMillis = System.currentTimeMillis();
				numberOfHungryTurns++;

				/*
				 * Tries actively to obtain both chopsticks and stops if interrupted
				 */
				while (hungry(currentTimeMillis) && !interrupted) {
					if (Thread.interrupted()) {
						/**
						 * The state of this Thread (interrupted) is changed to true, meaning that it was 
						 * interrupted and that this philosopher will no longer continue existing after 
						 * the following block.
						 */
						printDebugMessage("Philosopher " + id + " has finished");
						interrupted = true;
					}
				}
			}

			/*
			 * Eating state
			 * 
			 * At this point in the program, the philosopher has managed to obtain 
			 * both chopsticks and is about to enter the eating state if not interrupted
			 */
			if (!interrupted)
				eat();

		} while (!interrupted);
	}

	/*
	 * Help methods
	 */
	private void think() {
		int millisToThink = randomGenerator.nextInt(1000);
		try {
			printDebugMessage("Philosopher " + id + " is thinking for " + millisToThink);

			Thread.sleep(millisToThink);
			numberOfThinkingTurns++;
			thinkingTime += millisToThink;
		} catch (InterruptedException e) {
			/*
			 * The philosopher has been interrupted while thinking and he/she immediately 
			 * stops and increments the number of times he/she has has been thinking.
			 * 
			 * The state of this Thread (interrupted) is also changed to true, meaning that it was 
			 * interrupted and that this philosopher will no longer continue existing after 
			 * the following block.
			 */

			numberOfThinkingTurns++;
			printDebugMessage("Philosopher " + id + " has finished");

			interrupted = true;
		}
	}

	private boolean hungry(long hungryFrom) {
		boolean isHungry = true;
		if (leftChopstick.getLock().tryLock()) {
			if (rightChopstick.getLock().tryLock()) {
				this.hungryTime += (System.currentTimeMillis() - hungryFrom);
				printDebugMessage("Philosopher " + id + " was hungry for " + (System.currentTimeMillis() - hungryFrom));
				printDebugMessage(
						"Philosopher " + id + " picked up chopstick (" + leftChopstick.getId() + ") before eating");
				printDebugMessage(
						"Philosopher " + id + " picked up chopstick (" + rightChopstick.getId() + ") before eating");

				/*
				 * Both chopsticks are now obtained, and the philosopher is 
				 * no longer hungry and is ready to eat
				 */
				isHungry = false;
			} else {
				/*
				 * The philosopher did not manage to pick up the right chopstick, and
				 * he/she drops the left chopstick to avoid locking his/her neighbor to 
				 * the left if that neighbor has his/her right chopstick, which is this 
				 * philosopher's left chopstick. 
				 * 
				 * This philosopher tries to pick up the
				 * left chopstick again when this method is called again, which is just
				 * a few CPU-cycles later.
				 */
				leftChopstick.getLock().unlock();
			}
		}
		return isHungry;
	}

	private void eat() {
		/*
		 * At this point, the philosopher is still alive (Thread is running) and he/she
		 * has both chopsticks acquired in the hungry-method
		 */

		try {
			int millisToEat = randomGenerator.nextInt(1000);
			printDebugMessage("Philosopher " + id + " is eating for " + millisToEat);

			// Thread is put to sleep to illustrate the eating phase
			Thread.sleep(millisToEat);
			numberOfEatingTurns++;
			eatingTime += millisToEat;

			// Finished eating and drops both chopsticks.
			leftChopstick.getLock().unlock();
			printDebugMessage(
					"Philosopher " + id + " released left chopstick (" + leftChopstick.getId() + ") after eating");

			rightChopstick.getLock().unlock();
			printDebugMessage(
					"Philosopher " + id + " released right chopstick (" + rightChopstick.getId() + ") after eating");

		} catch (InterruptedException e) {

			/*
			 * The philosopher has been interrupted while eating and he/she immediately 
			 * drop both chopsticks (unlocks them) and increment the number of times he/she has 
			 * eaten, since the counting is usually done after he/she finishes eating (The philosopher
			 * never gets the chance to finish eating to then increment the number of times eaten). 
			 * 
			 * The state of this Thread (interrupted) is also changed to true, meaning that it was 
			 * interrupted and that this philosopher will no longer continue existing after 
			 * the following block.
			 */

			printDebugMessage("Philosopher " + id + " has finished");

			leftChopstick.getLock().unlock();
			printDebugMessage(
					"Philosopher " + id + " released left chopstick (" + leftChopstick.getId() + ") after eating");

			rightChopstick.getLock().unlock();
			printDebugMessage(
					"Philosopher " + id + " released right chopstick (" + rightChopstick.getId() + ") after eating");
			numberOfEatingTurns++;
			interrupted = true;
		}
	}

	private void printDebugMessage(String msg) {
		if (DEBUG) {
			System.out.println(msg);
		}
	}
}
