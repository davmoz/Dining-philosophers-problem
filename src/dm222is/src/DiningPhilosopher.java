package dm222is.src;

/*
 * File:	DiningPhilosopher.java
 * Course: 	Operating Systems
 * Code: 	1DV512
 * Author: 	Suejb Memeti (modified by Kostiantyn Kucher)
 * Date: 	November 2019
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DiningPhilosopher {

	/*
	 * Controls whether logs should be shown on the console or not. Logs should
	 * print events such as: state of the philosopher, and state of the chopstick
	 * for example: philosopher # is eating; philosopher # picked up the left
	 * chopstick (chopstick #)
	 */
	public boolean DEBUG = true;

	private final int NUMBER_OF_PHILOSOPHERS = 5;
	private int SIMULATION_TIME = 10000;
	private int SEED = 0;

	ExecutorService executorService = null;
	ArrayList<Philosopher> philosophers = null;
	ArrayList<Chopstick> chopsticks = null;

	public void start() throws InterruptedException {
		try {
			/*
			 * First we start two non-adjacent threads, which are T1 and T3
			 */
			for (int i = 1; i < NUMBER_OF_PHILOSOPHERS; i += 2) {
				executorService.execute(philosophers.get(i));
				Thread.sleep(50); // makes sure that this thread kicks in before the next one
			}

			/*
			 * Now we start the rest of the threads, which are T0, T2, and T4
			 */
			for (int i = 0; i < NUMBER_OF_PHILOSOPHERS; i += 2) {
				executorService.execute(philosophers.get(i));
				Thread.sleep(50); // makes sure that this thread kicks in before the next one
			}

			// Main thread sleeps till time of simulation
			Thread.sleep(SIMULATION_TIME);

			if (DEBUG) {
				System.out.println("\n>>> Asking all philosophers to stop\n");
			}

			/*
			 * The following call [ shutdownNow() ] will *interrupt* each executing task 
			 * (our runnable philosophers in this case). Once each philosopher is 
			 * interrupted, he/she will behave as following:
			 * 
			 * 1. If Thinking 	-> 	Stops thinking and terminate
			 * 2. If Hungry 	-> 	Stops being hungry and terminates
			 * 3. If Eating		-> 	Stops eating and release his/her chopsticks
			 */
			executorService.shutdownNow();

		} finally {
			executorService.shutdown();
			executorService.awaitTermination(10, TimeUnit.MILLISECONDS);
		}
	}

	public void initialize(int simulationTime, int randomSeed) {
		SIMULATION_TIME = simulationTime;
		SEED = randomSeed;

		philosophers = new ArrayList<Philosopher>(NUMBER_OF_PHILOSOPHERS);
		chopsticks = new ArrayList<Chopstick>(NUMBER_OF_PHILOSOPHERS);

		// create the executor service
		executorService = Executors.newFixedThreadPool(NUMBER_OF_PHILOSOPHERS);

		// Adding chopsticks to the chopsticks collection
		for (int i = 0; i < NUMBER_OF_PHILOSOPHERS; i++) {
			chopsticks.add(new Chopstick(i));
		}

		/*
		 * Adding new philosophers to the philosophers collection and assigning 
		 * them chopsticks with respect to their position at the table. 
		 * 
		 * e.g. Philosopher 0 will have his/her left chopstick at position (i+1) modulo 
		 * number of philosophers and his/her right chopstick at position i. 
		 * 
		 * Using the modulo operation because of the last philosopher. Could be done
		 * with if-statements instead but i chose to go with modulo. 
		 */
		for (int i = 0; i < NUMBER_OF_PHILOSOPHERS; i++) {
			philosophers.add(new Philosopher(i, chopsticks.get((i + 1) % NUMBER_OF_PHILOSOPHERS), chopsticks.get(i),
					SEED, DEBUG));
		}

	}

	public ArrayList<Philosopher> getPhilosophers() {
		return philosophers;
	}

	/*
	 * The following code prints a table where each row corresponds to one of the
	 * Philosophers, Columns correspond to the Philosopher ID (PID), average
	 * thinking time (ATT), average eating time (AET), average hungry time (AHT),
	 * number of thinking turns(#TT), number of eating turns(#ET), and number of
	 * hungry turns(#HT). This table should be printed regardless of the DEBUG value
	 */
	public void printTable() {
		DecimalFormat df2 = new DecimalFormat(".##");
		System.out.println("\n---------------------------------------------------");
		System.out.println("PID \tATT \tAET \tAHT \t#TT \t#ET \t#HT");

		for (Philosopher p : philosophers) {
			System.out.println(p.getId() + "\t" + df2.format(p.getAverageThinkingTime()) + "\t"
					+ df2.format(p.getAverageEatingTime()) + "\t" + df2.format(p.getAverageHungryTime()) + "\t"
					+ p.getNumberOfThinkingTurns() + "\t" + p.getNumberOfEatingTurns() + "\t"
					+ p.getNumberOfHungryTurns() + "\t");
		}

		System.out.println("---------------------------------------------------\n");
	}

}
