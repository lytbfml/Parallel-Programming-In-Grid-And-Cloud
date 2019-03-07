import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * @author Yangxiao on 12/13/2018.
 */
public class ss {
	
	
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		
		public void map(LongWritable key, Text value, OutputCollector<IntWritable, IntWritable> output, Reporter reporter) throws
				IOException {
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				output.collect(new IntWritable(Integer.parseInt(tokenizer.nextToken())), one);
			}
		}
	}
	
	public static class Reduce extends MapReduceBase implements Reducer<IntWritable, IntWritable, Text, IntWritable> {
		public void reduce(IntWritable key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws
				IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			output.collect(new Text("Sum = "), new IntWritable(sum));
		}
	}
}
