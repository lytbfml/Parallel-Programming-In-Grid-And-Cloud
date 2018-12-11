/**
 * @author Yangxiao on 12/8/2018.
 */


public class ACO {
	
	int[] BESTROUTE; // Best route in current iteration
	int[][] ROUTES; // The routes of all ants from 0 to ANTSIZE-1
	double[][] coordinates; // CITIES[i][j] stands for the distance between city i and city j
	double[][] PHEROMONES;// Pheromones on every edge.
	double[][] DELTA_PHEROMONES;
	double[][] PROBS;
	double[][] disMax; // distance between every cities
	private Randoms randoms;
	private int NUM_ANTS, NUM_CITIES, INITIAL_CITY;
	private double ALPHA, BETA, Q, RO, TAUMAX;
	private double BESTLENGTH;
	
	public ACO(int nAnts, int nCities, double alpha, double beta, double q, double ro,
	           double taumax, int initCity) {
		this.NUM_ANTS = nAnts;
		this.NUM_CITIES = nCities;
		this.ALPHA = alpha;
		this.BETA = beta;
		this.Q = q;
		this.RO = ro;
		this.TAUMAX = taumax;
		this.INITIAL_CITY = initCity;
		this.randoms = new Randoms(21);
	}
	
	
	public void init() {
		this.coordinates = new double[NUM_CITIES][2];
		this.PHEROMONES = new double[NUM_CITIES][NUM_CITIES];
		this.DELTA_PHEROMONES = new double[NUM_CITIES][NUM_CITIES];
		this.PROBS = new double[NUM_CITIES][2];
		
		this.disMax = new double[NUM_CITIES][NUM_CITIES];
		
		ROUTES = new int[NUM_ANTS][NUM_CITIES];
		BESTROUTE = new int[NUM_CITIES];
		for (int i = 0; i < NUM_CITIES; i++) {
			BESTROUTE[i] = -1;
			
			for (int j = 0; j < 2; j++) {
				PROBS[i][j] = -1.0;
			}
			
			for (int j = 0; j < NUM_CITIES; j++) {
				initial_Pheromones(i, j);
			}
		}
		
		for (int i = 0; i < NUM_ANTS; i++) {
			for (int j = 0; j < NUM_CITIES; j++) {
				ROUTES[i][j] = -1;
			}
		}
		
		BESTLENGTH = (double) Integer.MAX_VALUE;
	}
	
	public void optimize(int ITERATIONS) {
		
		for (int iterations = 1; iterations <= ITERATIONS; iterations++) {
			//System.out.println("ITERATION "+iterations+" HAS STARTED!");
			
			for (int k = 0; k < NUM_ANTS; k++) {
				//System.out.println(": ant "+k+" has been released!");
				while (0 != valid(k, iterations)) {
					//System.out.println(":: releasing ant "+k+" again!");
					for (int i = 0; i < NUM_CITIES; i++) {
						ROUTES[k][i] = -1;
					}
					route(k);
				}
				
				// for (int i = 0; i < NUM_CITIES; i++) {
				//System.out.print(ROUTES[k][i] +" ");
				// }
				
				//System.out.println("\n");
				//System.out.println( ":: route done");
				
				double rlength = length(k);
				
				if (rlength < BESTLENGTH) {
					BESTLENGTH = rlength;
					for (int i = 0; i < NUM_CITIES; i++) {
						BESTROUTE[i] = ROUTES[k][i];
					}
				}
				//System.out.println(": ant "+k+ " has ended!");
			}
			
			//System.out.println("updating PHEROMONES . . .");
			updatePHEROMONES();
			//cout << " done!" << endl << endl;
			//printPHEROMONES ();
			
			for (int i = 0; i < NUM_ANTS; i++) {
				for (int j = 0; j < NUM_CITIES; j++) {
					ROUTES[i][j] = -1;
				}
			}
			
			//cout << endl << "ITERATION " << iterations << " HAS ENDED!" << endl << endl;
		}
	}
	
	/**
	 * @param antk generate the route for antk
	 */
	private void route(int antk) {
		ROUTES[antk][0] = INITIAL_CITY;
		for (int i = 0; i < NUM_CITIES - 1; i++) {
			int city_i = ROUTES[antk][i];
			int count = 0;
			
			for (int city_j = 0; city_j < NUM_CITIES; city_j++) {
				if (city_i == city_j) {
					continue;
				}
				
				if (!visited(antk, city_j)) {
					PROBS[count][0] = PHI(city_i, city_j, antk);
					PROBS[count][1] = (double) city_j;
					count++;
				}
			}
			
			// deadlock
			if (0 == count) {
				return;
			}
			ROUTES[antk][i + 1] = city();
		}
	}
	
	private int city() {
		double xi = randoms.Uniforme();
		int i = 0;
		double sum = PROBS[i][0];
		while (sum < xi) {
			i++;
			sum += PROBS[i][0];
		}
		//todo
		return (int) PROBS[i][1];
	}
	
	private boolean visited(int antk, int city_j) {
		for (int l = 0; l < NUM_CITIES; l++) {
			if (ROUTES[antk][l] == -1) {
				break;
			}
			if (ROUTES[antk][l] == city_j) {
				return true;
			}
		}
		return false;
	}
	
	public void initial_Pheromones(int i, int j) {
		this.PHEROMONES[i][j] = randoms.Uniforme() * TAUMAX; // init random pheromones
		this.PHEROMONES[j][i] = PHEROMONES[i][j];
	}
	
	public void setCityPos(int city, double x, double y) {
		this.coordinates[city][0] = x;
		this.coordinates[city][1] = y;
	}
	
	private double distance(int i, int j) {
		return Math.hypot(coordinates[i][0] - coordinates[j][0],
		                  coordinates[i][1] - coordinates[j][1]);
	}
	
	private double distanceFromDisMax(int i, int j) {
		return disMax[i][j];
	}
	
	/**
	 * Generate CITIES distances for each city, total CITIES * CITIES distances
	 * @param coordinates (x, y) coordinates of CITIES different cities
	 */
	void formDisMax(int[][] coordinates) {
		for (int i = 0; i < NUM_CITIES; ++i) {
			for (int j = 0; j < NUM_CITIES; ++j) {
				disMax[i][j] = Math.hypot((coordinates[i][0] - coordinates[j][0]),
				                          (coordinates[i][1] - coordinates[j][1]));
			}
		}
	}
	
	private double length(int antk) {
		double sum = 0.0;
		for (int j = 0; j < NUM_CITIES; j++) {
			if (j == NUM_CITIES - 1) {
				sum += distanceFromDisMax(ROUTES[antk][j], ROUTES[antk][0]);
			} else {
				sum += distanceFromDisMax(ROUTES[antk][j], ROUTES[antk][j + 1]);
			}
		}
		return sum;
	}
	
	private double PHI(int cityi, int cityj, int antk) {
		double ETAij = Math.pow(1 / distanceFromDisMax(cityi, cityj), BETA);
		double TAUij = Math.pow(PHEROMONES[cityi][cityj], ALPHA);
		
		double sum = 0.0;
		for (int c = 0; c < NUM_CITIES; c++) {
			if (!visited(antk, c)) {
				double ETA = Math.pow(1 / distanceFromDisMax(cityi, c), BETA);
				double TAU = Math.pow(PHEROMONES[cityi][c], ALPHA);
				sum += ETA * TAU;
			}
		}
		return (ETAij * TAUij) / sum;
	}
	
	/**
	 * check the route of an ant
	 *
	 * @param antk
	 * @param iteration
	 * @return -1 means that there exists city not being reached;
	 * -2 means the path between not exist
	 * -3 means reach a city which has been reached already;
	 * -4 means the end of the trail not connected with source;
	 */
	private int valid(int antk, int iteration) {
		for (int i = 0; i < NUM_CITIES - 1; i++) {
			int cityi = ROUTES[antk][i];
			int cityj = ROUTES[antk][i + 1];
			if (cityi < 0 || cityj < 0) {
				return -1;
			}
			// if (!exists(cityi, cityj)) {
			// 	return -2;
			// }
			for (int j = 0; j < i - 1; j++) {
				if (ROUTES[antk][i] == ROUTES[antk][j]) {
					return -3;
				}
			}
		}
		
		// if (!exists(INITIALCITY, ROUTES[antk][NUMBEROFCITIES - 1])) {
		// 	return -4;
		// }
		
		return 0;
	}
	
	private void updatePHEROMONES() {
		for (int k = 0; k < NUM_ANTS; k++) {
			double rlength = length(k); // current path length for antk
			for (int r = 0; r < NUM_CITIES - 1; r++) {
				int cityi = ROUTES[k][r];
				int cityj = ROUTES[k][r + 1];
				DELTA_PHEROMONES[cityi][cityj] += Q / rlength;
				DELTA_PHEROMONES[cityj][cityi] += Q / rlength;
			}
		}
		for (int i = 0; i < NUM_CITIES; i++) {
			for (int j = 0; j < NUM_CITIES; j++) {
				PHEROMONES[i][j] = (1 - RO) * PHEROMONES[i][j] + DELTA_PHEROMONES[i][j];
				DELTA_PHEROMONES[i][j] = 0.0;
			}
		}
	}
	
	public void printPheromones() {
		System.out.println("PHEROMENES: ");
		System.out.print("|");
		for (int i = 0; i < NUM_CITIES; i++) {
			System.out.printf("%5d   ", i);
		}
		System.out.print("- |\n");
		for (int i = 0; i < NUM_CITIES; i++) {
			System.out.print(i + "|");
			for (int j = 0; j < NUM_CITIES; j++) {
				if (i == j) {
					System.out.printf("%5s   ", "x");
					continue;
				}
				System.out.printf("%7.3f ", PHEROMONES[i][j]);
			}
			System.out.println("\n");
		}
		System.out.println("\n");
	}
	
	public void printResults() {
		BESTLENGTH += distance(BESTROUTE[NUM_CITIES - 1], INITIAL_CITY);
		System.out.println(" BEST ROUTE:");
		for (int i = 0; i < NUM_CITIES; i++) {
			if (BESTROUTE[i] == 0) {
				System.out.println("source ");
				continue;
			}
			if (BESTROUTE[i] >= 1 && BESTROUTE[i] <= 26) {
				System.out.print((char) (BESTROUTE[i] - 1 + 'A'));
			} else {
				System.out.print((BESTROUTE[i] - 27));
			}
		}
		System.out.println("\n" + "length: " + BESTLENGTH);
	}
}
