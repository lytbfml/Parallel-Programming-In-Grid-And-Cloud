package edu.uwb.css534;

import edu.uw.bothell.css.dsl.MASS.*; // Library for Multi-Agent Spatial Simulation
import java.util.*;

public class Matrix extends Place {
	
	public Matrix() { }
	
	public Matrix(Object obj) { }
	
	public Object callMethod(int method, Object o) {
		
		switch (method) {
			
			default:
				return new String("Unknown Method Number: " + method);
			
		}
		
	}
}