/**
 * @author Yangxiao on 12/8/2018.
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.*;
import java.util.*;

public class ACO {
	
	double[][] coordinates; // CITIES[i][j] stands for the distance between city i and city j
	double[][] disMax;
	private int NUM_CITIES;
	
	void init() {
		disMax = new double[NUM_CITIES][NUM_CITIES];
	}
	
}
