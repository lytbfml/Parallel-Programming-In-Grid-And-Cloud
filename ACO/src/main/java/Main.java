/**
 * @author Yangxiao on 12/8/2018.
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.log;

public class Main {
	
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		
		JobConf conf = new JobConf(Main.class);
		conf.setJobName("ACO_TSP");
		
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		
		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);
		
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		
		JobClient.runJob(conf);
		System.out.println("Elapsed time = " + (System.currentTimeMillis() - time) + " ms");
	}
	
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		
		public static final double ALPHA = 0.5;
		public static final double BETA = 0.8;
		public static final double Q = 1000;
		public static final double RO = 0.5;
		public static final int TAUMAX = 2;
		public static final int INITIAL_CITY = 0;
		
		final int NUM_CITIES = 37;
		final int NUM_ITERATIONS = 1000;
		final int NUM_ANTS = 3;
		
		JobConf conf;
		int[][] coordinates = new int[NUM_CITIES][2];
		
		public void configure(JobConf job) {
			this.conf = job;
		}
		
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output,
		                Reporter reporter) throws
				IOException {
			ACO ants = new ACO(NUM_ANTS, NUM_CITIES, ALPHA, BETA, Q, RO, TAUMAX, INITIAL_CITY);
			ants.init();
			
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			int count = 0;
			// Create graph
			while (tokenizer.hasMoreTokens()) {
				tokenizer.nextToken();
				coordinates[count][0] = Integer.parseInt(tokenizer.nextToken());
				coordinates[count][1] = Integer.parseInt(tokenizer.nextToken());
				count++;
			}
			// calculate distance between each cities
			ants.formDisMax(coordinates);
			ants.optimize(NUM_ITERATIONS);
			String result = ants.printResults();
			output.collect(new Text("Result"), new Text(result));
		}
		
		public class ACO {
			
			int[] BESTROUTE; // Best route in current iteration
			int[][] ROUTES; // The routes of all ants from 0 to ANTSIZE-1
			double[][] coordinates;// CITIES[i][j] stands for the distance between city i and city j
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
					for (int k = 0; k < NUM_ANTS; k++) {
						while (0 != valid(k, iterations)) {
							for (int i = 0; i < NUM_CITIES; i++) {
								ROUTES[k][i] = -1;
							}
							route(k);
						}
						double rlength = length(k);
						if (rlength < BESTLENGTH) {
							BESTLENGTH = rlength;
							for (int i = 0; i < NUM_CITIES; i++) {
								BESTROUTE[i] = ROUTES[k][i];
							}
						}
					}
					updatePHEROMONES();
					for (int i = 0; i < NUM_ANTS; i++) {
						for (int j = 0; j < NUM_CITIES; j++) {
							ROUTES[i][j] = -1;
						}
					}
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
			 *
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
			
			public String printResults() {
				StringBuilder sb = new StringBuilder();
				BESTLENGTH += distance(BESTROUTE[NUM_CITIES - 1], INITIAL_CITY);
				sb.append(BESTLENGTH);
				System.out.println("BEST ROUTE: ");
				for (int i = 0; i < NUM_CITIES; i++) {
					if (BESTROUTE[i] == 0) {
						System.out.println("source ");
						sb.append(" source ");
						continue;
					}
					if (BESTROUTE[i] >= 1 && BESTROUTE[i] <= 26) {
						sb.append((char) (BESTROUTE[i] - 1 + 'A'));
					} else {
						sb.append((BESTROUTE[i] - 27));
					}
				}
				System.out.println(sb.toString());
				System.out.println("\n" + "length: " + BESTLENGTH);
				return sb.toString();
			}
		}
		
		public class Randoms {
			
			public static final int IA = 16807;
			public static final int IM = Integer.MAX_VALUE;
			public static final double AM = 1.0 / IM;
			public static final int IQ = 127773;
			public static final int IR = 2836;
			public static final int NTAB = 32;
			public static final double NDIV = 1 + (IM - 1) / NTAB;
			public static final double EPS = 1.2e-7;
			public static final double RNMX = 1.0 - EPS;
			
			long iy = 0;
			long iv[] = new long[NTAB];
			int iset = 0;
			
			double gset;
			private long[] xpto;
			
			private Random rand;
			
			public Randoms(long x) {
				this.xpto = new long[2];
				this.xpto[0] = -x;
				rand = new Random();
			}
			
			public double Normal(double avg, double sigma) {
				return avg + sigma * gaussdev(this.xpto);
			}
			
			public double Uniforme() { return ran1(this.xpto);}
			
			public double sorte(int m) { return ThreadLocalRandom.current().nextDouble(-m, m + 1); }
			
			public double ran1(long[] idum) {
				int j;
				long k;
				
				double temp;
				if (idum[0] <= 0 || iy == 0) {           // Initialize.
					if (-(idum[0]) < 1) {
						idum[0] = 1;     // Be sure to prevent idum = 0.
					} else { idum[0] = -(idum[0]); }
					for (
							j = NTAB + 7;
							j >= 0; j--) {      // Load the shuffle table (after 8 warm-ups).
						k = (idum[0]) / IQ;
						idum[0] = IA * (idum[0] - k * IQ) - IR * k;
						if (idum[0] < 0) { idum[0] += IM; }
						if (j < NTAB) { iv[j] = idum[0]; }
					}
					iy = iv[0];
				}
				
				k = (idum[0]) / IQ;                     // Start here when not initializing.
				idum[0] =
						IA * (idum[0] - k * IQ) -
								IR * k;       // Compute idum=(IA*idum) % IM without over-
				if (idum[0] < 0) {
					idum[0] += IM;       // flows by Schrage's method.
				}
				j = (int) (iy / NDIV);                        //  Will be in the range 0..NTAB-1.
				iy = iv[j];                         // Output previously stored value and refill the
				iv[j] = idum[0];                    // shuffle table.
				if ((temp = AM * iy) > RNMX) {
					return RNMX;                   // Because users don't expect endpoint values.
				} else { return temp; }
			}
			
			public double gaussdev(long[] idum) {
				double fac, rsq, v1, v2;
				if (idum[0] < 0) {
					iset = 0;      //     Reinitialize.
				}
				if (iset == 0) {            //     We don't have an extra deviate handy, so
					do {
						v1 = 2.0 * ran1(idum) -
								1.0;    // pick two uniform numbers in the square ex-
						v2 = 2.0 * ran1(idum) - 1.0;    // tending from -1 to +1 in each direction,
						rsq = v1 * v1 + v2 * v2;          // see if they are in the unit circle,
						
					} while (rsq >= 1.0 || rsq == 0.0);  // and if they are not, try again.
					fac = Math.sqrt(-2.0 * log(rsq) / rsq);
					// Now make the Box-Muller transformation to get two normal deviates.
					// Return one and save the other for next time.
					gset = v1 * fac;
					iset = 1;                 //  Set flag.
					return v2 * fac;
				} else {                    //   We have an extra deviate handy,
					iset = 0;                 //   so unset the flag,
					return gset;            //   and return it.
				}
			}
			
		}
	}
	
	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
		
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output,
		                   Reporter reporter) throws IOException {
			System.out.println();
			System.out.println("!!!!!!!!!!!!!!!!!REDUCER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println();
			
			HashMap<Integer, Text> map = new HashMap<>();
			double max = Double.MAX_VALUE;
			String result = "";
			
			while (values.hasNext()) {
				String re = values.next().toString();
				double len = Double.parseDouble(re.substring(0, re.indexOf("source") - 1));
				if (max > len) {
					max = len;
					result = re;
				}
			}
			System.out.println(result);
			output.collect(new Text("Result"), new Text(result));
		}
	}
}