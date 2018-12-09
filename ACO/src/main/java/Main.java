/**
 * @author Yangxiao on 12/8/2018.
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.*;
import java.util.*;

public class Main {
	
	////初始化configuration后，使用
	// conf.set("propertyName“，properyValue);
	//  //在mapper或reducer中，
	//  Configuration conf = context.getConfiguration();
	//  String g = conf.get("propertyName");
	//    //g作为可以使用的全局变量即可；
	
	
	public static final int NUM_CITIES = 37;
	public static final int NUM_ITERATIONS = 100000;
	public static final int NUM_ANTS = 18;
	double[][] coordinates = new double[NUM_CITIES][2]; // coordinates[i][j] stands for the distance between city i and city j
	
	
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		
		JobConf conf;
		
		public void configure(JobConf job) {
			this.conf = job;
		}
		
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws
				IOException {
			
			ACO ants = new ACO();
			ants.init();
			
			try {
				
				File file = new File("cities.txt");
				
				BufferedReader bf = new BufferedReader(new FileReader(file));
				
				String line;
				int i = 0;
				while ((line = bf.readLine()) != null) {
					String[] words = line.split("\t");
					
					for (int j = 0; j < NUM_CITIES; j++ ){
						if (i == j) continue;
					}
					
					i++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
		
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws
				IOException {
			
		}
	}
	
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		
		JobConf conf = new JobConf(ACO.class);
		conf.setJobName("ACO_TSP");
		
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		
		conf.setMapperClass(Map.class);
		//conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);
		
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		
		JobClient.runJob(conf);
		
		System.out.println("Elapsed time = " + (System.currentTimeMillis() - time) + " ms");
		
	}
	
	
	/**
	 * Generate CITIES distances for each city, total CITIES * CITIES distances
	 * @param coordinates (x, y) coordinates of CITIES different cities
	 * @param disMax Distance matrix
	 */
	public void formDisMax(int coordinates[CITIES][2], float disMax[CITIES][CITIES + 1]) {
		for (int i = 0; i < CITIES; ++i) {
			for (int j = 0; j < CITIES; ++j) {
				disMax[i][j] = hypot((coordinates[i][0] - coordinates[j][0]),
				                     (coordinates[i][1] - coordinates[j][1]));
			}
			disMax[i][CITIES] = hypot(coordinates[i][0] , coordinates[i][1]);
		}
	}
}
