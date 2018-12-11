package edu.uwb.css534;

import edu.uw.bothell.css.dsl.MASS.*; // Library for Multi-Agent Spatial Simulation
import java.util.*;

public class Worker extends Agent {
	
	public static final int goElsewhere_ = 0;
	
	
	public Worker() {
		super();
	}
	
	public Worker(Object object) {
		super();
	}
	
	public Object callMethod(int funcId) {
		switch (funcId) {
			case goElsewhere_:
				return goElsewhere(args);
		}
		return null;
	}
	
	
	public Object goElsewhere() {
		int newX = 0; // a new destination's X-coordinate
		int newY = 0; // a new destination's Y-coordinate
		int min = 1; // a new destination's # agents
		
		int currX = getPlace().getIndex()[0], currY = getPlace().getIndex()[1];
		int sizeX = getPlace().getSize()[0], sizeY = getPlace().getSize()[1];
		
		Random generator = new Random();
		boolean candidatePicked = false;
		
		int next = 0;
		next = generator.nextInt(1);
		if (next == 1) {
			newX = currX + generator.nextInt(sizeX - currX - 1);
		} else {
			newX = currX - generator.nextInt(currX);
		}
		
		next = generator.nextInt(1);
		if (next == 1) {
			newY = currY + generator.nextInt(sizeY - sizeY - 1);
		} else {
			newY = currY - generator.nextInt(currY);
		}
		
		migrate(newX, newY);
		return null;
	}
}
