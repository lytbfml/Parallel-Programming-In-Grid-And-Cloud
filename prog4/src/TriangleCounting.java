import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import sun.management.resources.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yangxiao on 12/13/2018.
 */
public class TriangleCounting {
	
	public static final String hasAgent = "ACTIVE";
	public static final String noAgent = "INACTIVE";
	
	
	public static void main(String[] args) {
		// start Sparks and read a given input file
		String inputFile = args[0];
		String nNodesString = args[1];
		int nNodes = Integer.parseInt(nNodesString);
		
		SparkConf conf = new SparkConf().setAppName("BFS-based Shortest Path Search");
		JavaSparkContext jsc = new JavaSparkContext(conf);
		JavaRDD<String> lines = jsc.textFile(inputFile);
		
		// build graph and initialize one agent at the final node
		JavaPairRDD<Integer, TriangleCounting.Data> network =
				lines.mapToPair(line -> {
					int indE = line.indexOf("=");
					String vID = line.substring(0, indE);
					int id = Integer.parseInt(vID);
					String[] lists = line.substring(indE + 1).split(";");
					int n = lists.length;
					List<Tuple2<Integer, Integer>> neighbors = new ArrayList<>();
					for (int i = 0; i < n; i++) {
						String[] parts = lists[i].split(",");
						neighbors.add(new Tuple2<>(Integer.parseInt(parts[0]),
						                           Integer.parseInt(parts[1])));
					}
					//let current node be the agent's starting node
					TriangleCounting.Data data1 = new TriangleCounting.Data(neighbors, hasAgent,
					                                                        new ArrayList<>(Arrays.asList(id)));
					return new Tuple2<>(id, data1);
				});
		
		int iteration = 0;
		
		while (iteration < 3) {
			JavaPairRDD<Integer, TriangleCounting.Data> propagatedNetwork;
			if (iteration == 2) {
				propagatedNetwork =
						network.flatMapToPair(vertex -> {
							List<Tuple2<Integer, Data>> list = new ArrayList<>();
							if (vertex._2.hasAgent.equals(hasAgent)) {
								for (Tuple2<Integer, Integer> neighbor : vertex._2.neighbors) {
									//if(the node's agents can go back to the starting node, add it to the result list)
									if (vertex._2.agant.contains(neighbor._1)) {
										list.add(new Tuple2<>(neighbor._1,
										                      new TriangleCounting.Data(
												                      new ArrayList<>(),
												                      hasAgent, vertex._2.agant)));
									}
								}
							}
							return list.iterator();
						});
				System.out.println("result = " + propagatedNetwork.count());
				
			} else {
				propagatedNetwork =
						network.flatMapToPair(vertex -> {
							List<Tuple2<Integer, Data>> list = new ArrayList<>();
							list.add(new Tuple2<>(vertex._1,
							                      new Data(vertex._2.neighbors, noAgent, new ArrayList<>())));
							if (vertex._2.hasAgent.equals(hasAgent)) {
								for (Tuple2<Integer, Integer> neighbor : vertex._2.neighbors) {
									//if current node has neighbor with smaller id spawn child and move the agent, otherwise, terminate
									if (neighbor._1 < vertex._1) {
										list.add(new Tuple2<>(neighbor._1,
										                      new TriangleCounting.Data(new ArrayList<>(), hasAgent, vertex._2.agant)));
									} else {
										list.add(new Tuple2<>(neighbor._1,
										                      new TriangleCounting.Data(new ArrayList<>(), noAgent, new ArrayList<>())));
									}
								}
							}
							return list.iterator();
						});
			}
			
			network = propagatedNetwork.reduceByKey((k1, k2) -> {
				
				List<Tuple2<Integer, Integer>> neighbors =
						k1.neighbors.size() == 0 ? k2.neighbors : k1.neighbors;
				ArrayList<Integer> aga = new ArrayList<>();
				//migrate all agents
				String agent = (k1.hasAgent.equals(hasAgent) || k2.hasAgent.equals(hasAgent))? hasAgent : noAgent ;
				if (agent.equals(hasAgent)) {
					if (k1.hasAgent.equals(hasAgent)) {
						aga.addAll(k1.agant);
					}
					if (k2.hasAgent.equals(hasAgent)) {
						aga.addAll(k2.agant);
					}
				}
				return new TriangleCounting.Data(neighbors, agent, aga);
			});
			
			iteration++;
		}
	}
	
	/**
	 * Vertex Attributes
	 */
	static class Data implements Serializable {
		List<Tuple2<Integer, Integer>> neighbors; // <neighbor0, weight0>, ...
		String hasAgent; // "INACTIVE" or "ACTIVE"
		ArrayList<Integer> agant = new ArrayList<>();
		public Data() {
			neighbors = new ArrayList<>();
			hasAgent = "INACTIVE";
		}
		
		public Data(List<Tuple2<Integer, Integer>> neighbors, String hasAgent, ArrayList<Integer> agant) {
			if (neighbors != null) {
				this.neighbors = new ArrayList<>(neighbors);
			} else {
				this.neighbors = new ArrayList<>();
			}
			this.hasAgent = hasAgent;
			this.agant = agant;
		}
	}
}
