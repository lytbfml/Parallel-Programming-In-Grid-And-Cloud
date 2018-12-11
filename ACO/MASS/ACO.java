package edu.uwb.css534;


import edu.uw.bothell.css.dsl.MASS.Agent;

/**
 * Created by liyangde on Dec, 2018
 */
public class ACO extends Agent {
    public static final int INIT = 0;
    public static final int CONNECT = 1;
    public static final int SET_POSITION = 2;
    public static final int OPTIMIZE = 3;

    static int[] BESTROUTE; // Best route in current iteration
    int [][] GRAPH;  // The adjacency matrix, if city i and city j has a path connected, the value in GRAPH[i][j] should be 1; else be 0;
    int [] ROUTES; // The routes of all ants from 0 to ANTSIZE-1
    static double [][] CITIES; // CITIES[i][j] stands for the distance between city i and city j
    static double [][] PHEROMONES;// Pheromones on every edge.
    double [][] DELTAPHEROMONES;
    double [][] PROBS;

    private static double BESTLENGTH;
    private Randoms randoms;

    public ACO (Object o) {
        randoms = new Randoms(21);
    }
    @Override
    public Object callMethod(int functionId, Object argument) {
        switch (functionId) {
            case INIT: return init();
            case CONNECT: return connectCITIES( argument);
            case SET_POSITION: return setCITYPOSITION(argument);
            case OPTIMIZE: return optimize();
            default:
                return "Unknown Method Number: " + functionId;
        }
    }

    public Object connectCITIES(Object data) {
        Integer[] cities = (Integer[]) data;
        int cityi = cities[0];
        int cityj = cities[1];
        this.GRAPH[cityi][cityj] = 1;
        this.PHEROMONES[cityi][cityj] = randoms.Uniforme() * ACO_Mass.TAUMAX; // init random pheromones
        this.GRAPH[cityj][cityi] = 1;
        this.PHEROMONES[cityj][cityi] = this.PHEROMONES[cityi][cityj];
        return null;
    }

    public Object setCITYPOSITION(Object data) {
        Double[] d = (Double[]) data;
        int city = d[0].intValue();
        double x = d[1];
        double y = d[2];
        CITIES[city][0] = x;
        CITIES[city][1] = y;
        return null;
    }

    public Object init() {
        this.GRAPH = new int[ACO_Mass.NUMBEROFCITIES][];
        this.CITIES = new double[ACO_Mass.NUMBEROFCITIES][];
        this.PHEROMONES = new double[ACO_Mass.NUMBEROFCITIES][];
        this.DELTAPHEROMONES = new double[ACO_Mass.NUMBEROFCITIES][];
        this.PROBS = new double[ACO_Mass.NUMBEROFCITIES][];
        for (int i = 0; i < ACO_Mass.NUMBEROFCITIES; i++) {
            this.GRAPH[i] = new int[ACO_Mass.NUMBEROFCITIES];
            this.CITIES[i] = new double[2];
            this.PHEROMONES[i] = new double[ACO_Mass.NUMBEROFCITIES];
            this.DELTAPHEROMONES[i] = new double[ACO_Mass.NUMBEROFCITIES];
            this.PROBS[i] = new double[2];
            for (int j = 0; j < 2; j++) {
                CITIES[i][j] = -1.0;
                PROBS[i][j] = -1.0;
            }
            for (int j = 0; j < ACO_Mass.NUMBEROFCITIES; j++) {
                GRAPH[i][j] = 0;
                PHEROMONES[i][j] = 0.0;
                DELTAPHEROMONES[i][j] = 0.0;
            }
        }

        ROUTES = new int[ACO_Mass.NUMBEROFCITIES];
        for (int j = 0; j < ACO_Mass.NUMBEROFCITIES; j++) {
            ROUTES[j] = -1;
        }


        BESTLENGTH = (double) Integer.MAX_VALUE;
        BESTROUTE = new int[ACO_Mass.NUMBEROFCITIES];
        for (int i = 0; i < ACO_Mass.NUMBEROFCITIES; i++) {
            BESTROUTE[i] = -1;
        }

        return null;
    }


    public Object optimize() {

        while (0 != valid()) {
            //System.out.println(":: releasing ant "+k+" again!");
            for (int i = 0; i < ACO_Mass.NUMBEROFCITIES; i++) {
                ROUTES[i] = -1;
            }
            route();
        }

        double rlength = length();

        if (rlength < BESTLENGTH) {
            BESTLENGTH = rlength;
            for (int i = 0; i < ACO_Mass.NUMBEROFCITIES; i++) {
                BESTROUTE[i] = ROUTES[i];
            }
        }

        updatePHEROMONES();

        for (int j = 0; j < ACO_Mass.NUMBEROFCITIES; j++) {
            ROUTES[j] = -1;
        }

        return null;
    }

    private double length() {
        double sum = 0.0;
        for (int j = 0; j < ACO_Mass.NUMBEROFCITIES; j++) {
            if (j == ACO_Mass.NUMBEROFCITIES - 1) {
                sum += distance(ROUTES[j], ROUTES[0]);
            } else {
                sum += distance(ROUTES[j], ROUTES[j + 1]);
            }
        }
        return sum;
    }

    private void route() {
        ROUTES[0] = ACO_Mass.INITIALCITY;
        for (int i = 0; i < ACO_Mass.NUMBEROFCITIES - 1; i++) {
            int cityi = ROUTES[i];
            int count = 0;
            for (int c = 0; c < ACO_Mass.NUMBEROFCITIES; c++) {
                if (cityi == c) {
                    continue;
                }

                if (!visited(c)) {
                    PROBS[count][0] = PHI(cityi,c);
                    PROBS[count][1] = (double) c;
                    count++;
                }
            }

            // deadlock
            if (0 == count) {
                return;
            }

            ROUTES[i + 1] = city();
        }
    }

    private int valid() {
        for (int i = 0; i < ACO_Mass.NUMBEROFCITIES - 1; i++) {
            int cityi = ROUTES[i];
            int cityj = ROUTES[i + 1];
            if (cityi < 0 || cityj < 0) {
                return -1;
            }
            for (int j = 0; j < i - 1; j++) {
                if (ROUTES[i] == ROUTES[j]) {
                    return -3;
                }
            }
        }
        return 0;
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

    private double PHI(int cityi, int cityj) {
        double ETAij = Math.pow(1 / distance(cityi, cityj), ACO_Mass.BETA);
        double TAUij = Math.pow(PHEROMONES[cityi][cityj], ACO_Mass.ALPHA);

        double sum = 0.0;
        for (int c = 0; c < ACO_Mass.NUMBEROFCITIES; c++) {
            if (!visited(c)) {
                double ETA = Math.pow(1 / distance(cityi, c), ACO_Mass.BETA);
                double TAU = Math.pow(PHEROMONES[cityi][c], ACO_Mass.ALPHA);
                sum += ETA * TAU;
            }

        }
        return (ETAij * TAUij) / sum;
    }

    private boolean visited(int c) {
        for (int l = 0; l < ACO_Mass.NUMBEROFCITIES; l++) {
            if (ROUTES[l] == -1) {
                break;
            }
            if (ROUTES[l] == c) {
                return true;
            }
        }
        return false;
    }

    private static double  distance(int cityi, int cityj) {
        return Math.sqrt(Math.pow(CITIES[cityi][0] - CITIES[cityj][0], 2)
                + Math.pow(CITIES[cityi][1] - CITIES[cityj][1], 2));
    }

    private void updatePHEROMONES() {
        double rlength = length(); // current path length for antk
        for (int r = 0; r < ACO_Mass.NUMBEROFCITIES - 1; r++) {
            int cityi = ROUTES[r];
            int cityj = ROUTES[r + 1];
            DELTAPHEROMONES[cityi][cityj] += ACO_Mass.Q / rlength;
            DELTAPHEROMONES[cityj][cityi] += ACO_Mass.Q / rlength;
        }
        for (int i = 0; i < ACO_Mass.NUMBEROFCITIES; i++) {
            for (int j = 0; j < ACO_Mass.NUMBEROFCITIES; j++) {
                PHEROMONES[i][j] = (1 - ACO_Mass.RO) * PHEROMONES[i][j] + DELTAPHEROMONES[i][j];
                DELTAPHEROMONES[i][j] = 0.0;
            }
        }
    }

    public static void printRESULTS() {
        BESTLENGTH += distance(BESTROUTE[ACO_Mass.NUMBEROFCITIES - 1], ACO_Mass.INITIALCITY);
        System.out.println(" BEST ROUTE:");
        for (int i = 0; i < ACO_Mass.NUMBEROFCITIES; i++) {
            if (BESTROUTE[i] == 0) {
                System.out.println("source ");
                continue;
            }
            if (BESTROUTE[i] >=1 && BESTROUTE[i] <= 26) {
                System.out.print((char) (BESTROUTE[i] - 1 +'A'));
            } else {
                System.out.print((BESTROUTE[i] - 27));
            }
        }
        System.out.println("\n" + "length: " + BESTLENGTH);
    }
}
