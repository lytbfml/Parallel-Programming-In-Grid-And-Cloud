import java.util.Arrays;
 
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
 
public class JavaSparkFib {
 
    public static void main(String[] args) {
        // configure spark
        SparkConf sparkConf = new SparkConf().setAppName("Read Text to RDD")
	    .setMaster("local[2]").set("spark.executor.memory","2g");
        // start a spark context
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        
        // initialize an integer RDD
        JavaRDD<Integer> numbers = sc.parallelize(Arrays.asList(3, 4, 5, 6, 7));
        
        // map each line to number of words in the line
        JavaRDD<Integer> fib_values 
	    = numbers.map(
			  new Function<Integer, Integer>() {
			      private static final long serialVersionUID = 1L;
			      @Override
			      public Integer call( Integer v1 ) throws Exception {
				  int previous = 1;
				  int current = 1;
				  int next = 1;
				  for ( int i = 3; i <= v1; i++ ) {
				      next = current + previous;
				      previous = current;
				      current = next;
				  }
				  return next;
			      }
			  }
			  );
        
        // collect RDD for printing
        for(double value:fib_values.collect()){
            System.out.println(value);
        }
    }
}
