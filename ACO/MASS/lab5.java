/**
 * @author Yangxiao on 12/10/2018.
 */
package edu.uwb.css534;

import edu.uw.bothell.css.dsl.MASS.*; // Library for Multi-Agent Spatial Simulation
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
		Places matrix = new Place( 1, "Matrix", null, 10, 10);
		Agents worker = new Agents( 2, "Worker", null, matrix, 2);
		worker.callAll( Worker.goElsewhere_ );
		worker.manageAll( );
		MASS.finish();
	}
}


