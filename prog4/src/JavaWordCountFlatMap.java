/**
 * @author Yangxiao on 11/21/2018.
 */
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.Arrays;
import java.util.Iterator;

public class JavaWordCountFlatMap {
	
	public static void main(String[] args) {
		// configure spark
		SparkConf sparkConf = new SparkConf().setAppName("Java Word Count FlatMap")
				.setMaster("local[2]").set("spark.executor.memory", "2g");
		// start a spark context
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		
		// provide path to input text file
		String path = "sample.txt";
		
		// read text file to RDD
		JavaRDD<String> lines = sc.textFile(path);
		
		// Java 8 with lambdas: split the input string into words
		JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
			@Override
			public Iterator<String> call(String s) throws Exception {
				return Arrays.asList( s.split( " " ) ).iterator();
			}
		});
		
		// print #words
		System.out.println("#words = " + words.count());
	}
	
}