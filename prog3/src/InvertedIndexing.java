/**
 * @author Yangxiao on 11/2/2018.
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.*;


public class InvertedIndexing {
	
	
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		
		JobConf conf;
		
		public void configure(JobConf job) {
			this.conf = job;
		}
		
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws
				IOException {
			
			// retrieve # keywords from JobConf
			int argc = Integer.parseInt(conf.get("argc"));
			// put args into a String array
			Set<String> args = new HashSet();
			// retrieve keywords
			for (int i = 0; i < argc; i++) {
				args.add(conf.get("keyword" + i));
			}
			// get the current file name
			FileSplit fileSplit = (FileSplit) reporter.getInputSplit();
			String filename = "" + fileSplit.getPath().getName();
			String lines = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(lines);
			//collect if next token match one of the args
			while (tokenizer.hasMoreTokens()) {
				String x = tokenizer.nextToken();
				if (args.contains(x)) {
					output.collect(new Text(x), new Text(filename));
				}
			}
		}
	}
	
	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
		
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws
				IOException {
			HashMap<String, Integer> hm = new HashMap<String, Integer>();
			//Count the occurrence number of key in each file
			while (values.hasNext()) {
				String name = values.next().toString();
				if (hm.containsKey(name)) {
					hm.put(name, hm.get(name) + 1);
				} else {
					hm.put(name, 1);
				}
			}
			//create Comparator to sort the result by count number
			Comparator<java.util.Map.Entry<String, Integer>> valueComparator =
					new Comparator<java.util.Map.Entry<String, Integer>>() {
						@Override
						public int compare(java.util.Map.Entry<String, Integer> e1, java.util.Map.Entry<String, Integer> e2) {
							int v1 = e1.getValue();
							int v2 = e2.getValue();
							return v1 - v2;
						}
					};
			
			//sort the result
			List<java.util.Map.Entry<String, Integer>> listDoc =
					new ArrayList<java.util.Map.Entry<String, Integer>>(hm.entrySet());
			Collections.sort(listDoc, valueComparator);
			
			//create output string
			StringBuilder sb = new StringBuilder();
			for (java.util.Map.Entry<String, Integer> e : listDoc) {
				sb.append(e.getKey());
				sb.append(" ");
				sb.append(e.getValue());
				sb.append(" ");
			}
			
			//output
			Text docListC = new Text(sb.toString());
			output.collect(key, docListC);
		}
	}
	
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		
		JobConf conf = new JobConf(InvertedIndexing.class);
		conf.setJobName("invertInd");
		
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		
		conf.setMapperClass(Map.class);
		//conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);
		
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		
		conf.set("argc", String.valueOf(args.length - 2)); // argc maintains #keywords
		for (int i = 0; i < args.length - 2; i++) {
			conf.set("keyword" + i, args[i + 2]);
		}
		
		JobClient.runJob(conf);
		
		System.out.println("Elapsed time = " + (System.currentTimeMillis() - time) + " ms");
	}
}
