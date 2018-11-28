import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileInputStream;

public class LinkPrediction {

    public static void main( String[] args ) {
	// Start Spark
        SparkConf conf = new SparkConf( ).setAppName( "Link Prediction" );
        JavaSparkContext sc = new JavaSparkContext( conf );

	// Unweighted adjacency matrix: 
	// Each JavaRDD represents a different node's adjacency list.
	List<JavaRDD<Integer>> sets = new ArrayList<>( );
	try {
	    Scanner file = new Scanner( new FileInputStream( args[0] ) );
	    for ( int i = 0; file.hasNextLine( ); i++ ) {
		Scanner line = new Scanner( file.nextLine( ) );
		List<Integer> ints = new ArrayList<>( );
		int dest = 0;
		while ( line.hasNextInt( ) ) {
		    int weight = line.nextInt( );
		    if ( weight > 0 ) 
			ints.add( dest );
		    dest++;
		}
		sets.add( sc.parallelize( ints ) );
	    }
	} catch ( IOException e ) {
	    e.printStackTrace( );
	    System.exit( -1 );
	}

	// Show each node's adjacency list (connectivity).
	for( int i = 0; i < sets.size( ); i++ ) {
	    System.err.print( "set[" + i + "]" );
	    printItr( sets.get(i).collect( ).iterator( ) );
	}

	List<JavaPairRDD<Integer,Integer>> weights = new ArrayList<>( );
	try {
	    Scanner file = new Scanner( new FileInputStream( args[0] ) );
	    for ( int i = 0; file.hasNextLine( ); i++ ) {
		Scanner line = new Scanner( file.nextLine( ) );
		List<Tuple2<Integer,Integer>> destWeightPairs = new ArrayList<>( );
		int dest = 0;
		while ( line.hasNextInt( ) ) {
		    int weight = line.nextInt( );
		    if ( weight > 0 ) 
			destWeightPairs.add( new Tuple2( dest, weight ) );
		    dest++;
		}
		weights.add( sc.parallelizePairs( destWeightPairs ) );
	    }
	} catch ( IOException e ) {
	    e.printStackTrace( );
	    System.exit( -1 );
	}
	
	// Show each node's adjacency list (weights).
	for( int i = 0; i < weights.size( ); i++ ) {
	    System.err.print( "weight[" + i + "]" );
	    printItr( weights.get(i).collect( ).iterator( ) );
	}
	
	int nNodes = sets.size( );
	
	List<Tuple2<Double, Tuple2<Integer,Integer>>> scoresA = new ArrayList<>( );
	List<Tuple2<Double, Tuple2<Integer,Integer>>> scoresJ = new ArrayList<>( );
	List<Tuple2<Double, Tuple2<Integer,Integer>>> scoresWA = new ArrayList<>( );
	List<Tuple2<Integer, Tuple2<Integer,Integer>>> scoresWC = new ArrayList<>( );
	for ( int i = 0; i < nNodes; i++ ) {
	    for ( int j = i + 1; j < nNodes; j++ ) {
		JavaRDD<Integer> intersect = sets.get(i).intersection(sets.get(j));
		JavaRDD<Integer> union = sets.get(i).union(sets.get(j)).distinct();
		if ( intersect.count( ) > 0 ) {
		    System.err.print( "AdamicAdar score[" + i + "][" + j + "] = ");
		    System.err.println( scoreAdamicAdar( sets, intersect ) );
		    scoresA.add( new Tuple2<Double, Tuple2<Integer,Integer>>
				( scoreAdamicAdar( sets, intersect ), 
				  new Tuple2<Integer,Integer>( i, j ) )
				  );

		    System.err.print( "Jaccard   score[" + i + "][" + j + "] = ");
		    System.err.println( scoreJaccard( intersect, union ) );
		    scoresJ.add( new Tuple2<Double, Tuple2<Integer,Integer>>
				 ( scoreJaccard( intersect, union ), 
				  new Tuple2<Integer,Integer>( i, j ) )
				  );

		    System.err.print( "Weighted AdamicAdar score[" + i + "][" + j + "] = ");
		    JavaPairRDD<Integer,Tuple2<Integer,Integer>> joinedWeights
			= weights.get(i).join(weights.get(j));
		    printItr( joinedWeights.collect( ).iterator( ) );
		    System.err.println( scoreWeightedAdamicAdar( sets, joinedWeights ) );
		    scoresWA.add( new Tuple2<Double, Tuple2<Integer,Integer>>
				  ( scoreWeightedAdamicAdar( sets, joinedWeights ), 
				    new Tuple2<Integer,Integer>( i, j ) )
				  );
		    
		    System.err.print( "Weighted Common Neighbor score[" + i + "][" + j + "] = ");
		    System.err.println( scoreWeightedCommonN( joinedWeights ) );
		    scoresWC.add( new Tuple2<Integer, Tuple2<Integer,Integer>>
				  ( scoreWeightedCommonN( joinedWeights ), 
				    new Tuple2<Integer,Integer>( i, j ) )
				  );
		    
		    System.err.println( );
		}
	    }
	}
	JavaPairRDD<Double, Tuple2<Integer,Integer>> scoresA_RDD = sc.parallelizePairs( scoresA );
	JavaPairRDD<Double, Tuple2<Integer,Integer>> sortedScoresA_RDD = scoresA_RDD.sortByKey( false );
	System.err.println( "*** Best Adamic Adar Score = " + sortedScoresA_RDD.first( ) );

	JavaPairRDD<Double, Tuple2<Integer,Integer>> scoresJ_RDD = sc.parallelizePairs( scoresJ );
	JavaPairRDD<Double, Tuple2<Integer,Integer>> sortedScoresJ_RDD = scoresJ_RDD.sortByKey( false );
	System.err.println( "*** Best Jaccard     Score = " + sortedScoresJ_RDD.first( ) );

	JavaPairRDD<Double, Tuple2<Integer,Integer>> scoresWA_RDD = sc.parallelizePairs( scoresWA );
	JavaPairRDD<Double, Tuple2<Integer,Integer>> sortedScoresWA_RDD = scoresWA_RDD.sortByKey( false );
	System.err.println( "*** Best Weighted Adamic Adar Score = " + sortedScoresWA_RDD.first( ) );

	JavaPairRDD<Integer, Tuple2<Integer,Integer>> scoresWC_RDD = sc.parallelizePairs( scoresWC );
	JavaPairRDD<Integer, Tuple2<Integer,Integer>> sortedScoresWC_RDD = scoresWC_RDD.sortByKey( false );
	System.err.println( "*** Best Weighted Common Neighbors Score = " + sortedScoresWC_RDD.first( ) );

	// Stop spark
	sc.stop( );
    }

    private static double scoreAdamicAdar( List<JavaRDD<Integer>> sets, JavaRDD<Integer> intersect ) {
	double nXY = intersect.count( );
	
	Iterator itr = intersect.collect( ).iterator( );
	int nZ = 0;
	while ( itr.hasNext( ) ) {
	    Object element = itr.next( );
	    nZ += sets.get( ((Integer)(element)).intValue( ) ).count( );
	}
	return nXY / Math.log( 1 + nZ );
	//return nXY / nZ;
    }

    private static double scoreJaccard( JavaRDD<Integer> intersect, JavaRDD<Integer> union ) {
	return (double)( intersect.count( ) )/ union.count( );
    }

    private static double scoreWeightedAdamicAdar( List<JavaRDD<Integer>> sets, 
						   JavaPairRDD<Integer, Tuple2<Integer,Integer>> weightedUnion ) {
	double nXY = scoreWeightedCommonN( weightedUnion );
	
	JavaRDD<Integer> keys = weightedUnion.keys();
	Iterator itr = keys.collect( ).iterator( );
	int nZ = 0;
	while ( itr.hasNext( ) ) {
	    Object element = itr.next( );
	    nZ += sets.get( ((Integer)(element)).intValue( ) ).count( );
	}
	return nXY / Math.log( 1 + nZ );
	//return nXY / nZ;
    }

    private static int scoreWeightedCommonN( JavaPairRDD<Integer,Tuple2<Integer,Integer>> weightedUnion ) {
	JavaRDD<Tuple2<Integer,Integer>> values = weightedUnion.values();
	JavaRDD<Integer> distances = values.map( s -> s._1 + s._2 );
	return distances.reduce( ( s1, s2 ) -> s1 + s2 );
    }
    
    public static void printItr( Iterator itr ) {
	while(itr.hasNext()) {
	    Object element = itr.next();
	    System.err.print( element + " " );
	}
	System.err.println( );
    }

    public static void printItrln( Iterator itr ) {
	while(itr.hasNext()) {
	    Object element = itr.next();
	    System.err.println( element );
	}
    }
}