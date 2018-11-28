import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

public class JavaWordCountPairRDD {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
	    .setAppName("Word Count");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> textFile = sc.textFile("sample.txt");
        JavaRDD<String> words = textFile.flatMap(s -> Arrays.asList( s.split( " " ) ).iterator() );
        JavaPairRDD<String, Long> pairs =
	    words.mapToPair(s -> new Tuple2<>(s, 1L));
        JavaPairRDD<String, Long> counts =
	    pairs.reduceByKey((Function2<Long, Long, Long>) (a, b) -> a + b);

        Iterator itr = counts.collect().iterator();
      
	while(itr.hasNext()) {
	    Object element = itr.next();
	    System.out.println( element );
	}
    }
}
