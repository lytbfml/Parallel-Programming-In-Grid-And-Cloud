/**
 * @author Yangxiao on 12/10/2018.
 */
package edu.uwb.css534;

import edu.uw.bothell.css.dsl.MASS.*; // Library for Multi-Agent Spatial Simulation
import edu.uw.bothell.css.dsl.MASS.logging.LogLevel;
import edu.uw.bothell.css.dsl.MASS.Agents;
import edu.uw.bothell.css.dsl.MASS.MASS;
import edu.uw.bothell.css.dsl.MASS.Places;
import java.util.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;

public class lab5 {
	
	private static final String NODE_FILE = "nodes.xml";
	
	
	public static void main(String[] args) throws Exception {
		
		
		MASS.setNodeFilePath(NODE_FILE);
		MASS.setLoggingLevel(LogLevel.DEBUG);
		MASS.init();
		
		Places matrix = new Places( 1, Matrix.class.getName(), null, 10, 10);
		Agents worker = new Agents( 2, Worker.class.getName(), null, matrix, 2);
		
		worker.callAll( Worker.goElsewhere_ );
		worker.manageAll( );
		
		MASS.finish();
	}
	
}