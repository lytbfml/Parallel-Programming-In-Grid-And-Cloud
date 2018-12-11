package edu.uwb.css534;


import edu.uw.bothell.css.dsl.MASS.Agents;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by liyangde on Dec, 2018
 */
public class ACO_Mass {
    public static final int ITERATIONS = 10000;
    public static final int NUMBEROFANTS = 4;
    public static final int NUMBEROFCITIES = 37;

    public static final double ALPHA = 0.5;
    public static final double BETA  = 0.8;
    public static final double Q     = 1000;
    public static final double RO    = 0.5;
    public static final int    TAUMAX = 2;
    public static final int    INITIALCITY = 0; //source



    public static void main(String[] args) {
        Agents ants = new Agents(1, ACO.class.getName(), null, null, NUMBEROFANTS);

        ants.callAll(ACO.INIT);

        // read from file
        try {

            File file = new File("cities.txt");

            BufferedReader bf = new BufferedReader(new FileReader(file));

            String line;
            int i = 0;
            while ((line = bf.readLine()) != null) {
                String[] words = line.split("\t");
                Object[] arg = Arrays.asList(i,Integer.parseInt(words[1]),Integer.parseInt(words[2])).toArray();
                ants.callAll(ACO.SET_POSITION, arg);
                for (int j = 0; j < NUMBEROFCITIES; j++ ){
                    if (i == j) continue;
                    ants.callAll(ACO.CONNECT, Arrays.asList(i,j).toArray());
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < ITERATIONS; i++) {
            // let ants go
            ants.callAll(ACO.OPTIMIZE);
            // update pheronmen
            ants.manageAll();
        }
    }
}
