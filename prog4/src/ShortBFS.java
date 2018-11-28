import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yangxiao on 11/20/2018.
 */

public class ShortBFS {
	
	public static final String ACTIVE = "ACTIVE";
	public static final String INACTIVE = "INACTIVE";
	
	public static void main(String[] args) {
		// start Sparks and read a given input file
		String inputFile = args[0];
		String start = args[1];
		String end = args[2];
		
		SparkConf conf = new SparkConf().setAppName("BFS-based Shortest Path Search");
		JavaSparkContext jsc = new JavaSparkContext(conf);
		JavaRDD<String> lines = jsc.textFile(inputFile);
		// now start a timer
		long startTime = System.currentTimeMillis();
		JavaPairRDD<String, Data> network =
				lines.mapToPair(line -> {
					int indE = line.indexOf("=");
					String vID = line.substring(0, indE);
					String[] lists = line.substring(indE + 1).split(";");
					int n = lists.length;
					List<Tuple2<String, Integer>> neighbors = new ArrayList<>();
					for (int i = 0; i < n; i++) {
						String[] parts = lists[i].split(",");
						neighbors.add(new Tuple2<>(parts[0], Integer.parseInt(parts[1])));
					}
					Data data1;
					if (vID.equals(start)) {
						data1 = new Data(neighbors, 0, 0, ACTIVE);
					} else {
						data1 = new Data(neighbors, Integer.MAX_VALUE, Integer.MAX_VALUE, INACTIVE);
					}
					return new Tuple2<>(vID, data1);
				});
		System.out.println("Count = " + network.count() + "\n");
		
		while (network.filter(f -> {
			if (f._2.status.equals(ACTIVE)) {
				return true;
			} else {
				return false;
			}
		}).count() > 0) {
			JavaPairRDD<String, Data> propagatedNetwork = network.flatMapToPair(vertex -> {
				// If a vertex is "ACTIVE", create Tuple2( neighbor, new Data() ) for
				// each neighbor where Data should include a new distance to this neighbor.
				// Add each Tuple2 to a list. Dont forget this vertex itself back to the
				// list. Return all the list items.
				List<Tuple2<String, Data>> list = new ArrayList<>();
				list.add(new Tuple2<>(vertex._1,
						new Data(vertex._2.neighbors, vertex._2.distance, vertex._2.prev,
								INACTIVE)));
				if (vertex._2.status.equals(ACTIVE)) {
					for (Tuple2<String, Integer> neighbor : vertex._2.neighbors) {
						list.add(new Tuple2<>(neighbor._1,
								new Data(new ArrayList<>(), (neighbor._2 + vertex._2.distance),
										Integer.MAX_VALUE, INACTIVE)));
					}
				}
				return list.iterator();
			});
			
			network = propagatedNetwork.reduceByKey((k1, k2) -> {
				// For each key, (i.e, each vertex), find the shortest distance and
				// update this vertex Data attribute.
				List<Tuple2<String, Integer>> neighbors =
						k1.neighbors.size() == 0 ? k2.neighbors : k1.neighbors;
				int dis = Math.min(k1.distance, k2.distance);
				int prev = Math.min(k1.prev, k2.prev);
				return new Data(neighbors, dis, prev, INACTIVE);
			});
			
			network = network.mapValues(value -> {
				// If a vertex new distance is shorter than prev, activate this vertex
				// status and replace prev with the new distance.
				if (value.distance < value.prev) {
					return new Data(value.neighbors, value.distance, value.distance, ACTIVE);
				}
				return value;
			});
			
			//network = propagatedNetwork.reduceByKey((k1, k2) -> {
			//				// For each key, (i.e, each vertex), find the shortest distance and
			//				// update this vertex Data attribute.
			//				List<Tuple2<String, Integer>> neighbors =
			//						k1.neighbors.size() == 0 ? k2.neighbors : k1.neighbors;
			//				int dis = Math.min(k1.distance, k2.distance);
			//				int prev = Math.min(k1.prev, k2.prev);
			//
			//				String status = (k1.status.equals(ACTIVE) || k2.status.equals(ACTIVE)) ? ACTIVE : INACTIVE;
			//
			//				if (dis < prev || status.equals(ACTIVE)) {
			//					return new Data(neighbors, dis, dis, ACTIVE);
			//				} else {
			//					return new Data(neighbors, dis, prev, INACTIVE);
			//				}
			//			});
		}
		
		List<Data> res = network.lookup(end);
		System.out.println(
				"from " + start + " to " + end + " takes distance = " + res.get(0).distance);
		System.out.println("Time = " + (System.currentTimeMillis() - startTime));
	}
	
	/**
	 * Vertex Attributes
	 */
	static class Data implements Serializable {
		List<Tuple2<String, Integer>> neighbors; // <neighbor0, weight0>, ...
		String status; // "INACTIVE" or "ACTIVE"
		Integer distance; // the distance so far from source to this vertex
		Integer prev; // the distance calculated in the previous iteration
		
		public Data() {
			neighbors = new ArrayList<>();
			status = "INACTIVE";
			distance = 0;
		}
		
		public Data(List<Tuple2<String, Integer>> neighbors,
		            Integer dist, Integer prev, String status) {
			if (neighbors != null) {
				this.neighbors = new ArrayList<>(neighbors);
			} else {
				this.neighbors = new ArrayList<>();
			}
			this.distance = dist;
			this.prev = prev;
			this.status = status;
		}
	}
}
