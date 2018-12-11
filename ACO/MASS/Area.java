import java.util.Vector;

/**
 * @author Yangxiao on 12/10/2018.
 */
public class Area extends Place {
	public static final int init_ = 0;
	public static final int computeWave_ = 1;
	public static final int exchangeWave_ = 2;
	public static final int collectWave_ = 3;
	
	// wave height at each cell
	// wave[0]: current, wave[1]: previous, wave[2]: one more previous height
	private final int north = 0, east = 1, south = 2, west = 3;
	private final double c = 1.0; // wave speed
	// wave height from four neighbors: north, east, south, and west
	private final double dt = 0.1; // time quantum
	private final double dd = 2.0; // change in system
	// simulation constants
	double[] wave = new double[3];
	int time = 0;
	double[] neighbors = new double[4];
	// the array size and my index in (x, y) coordinates
	private int sizeX, sizeY;
	private int myX, myY;
	
	public Area(Object args) {
		Vector<int[]> placeNeighbors = new Vector<int[]>();
		placeNeighbors.add(new int[]{0, -1});
		placeNeighbors.add(new int[]{1, 0});
		placeNeighbors.add(new int[]{0, 1});
		placeNeighbors.add(new int[]{-1, 0});
		setNeighbors(placeNeighbors);
	}
	
	public Object callMethod(int funcId, Object args) {
		switch (funcId) {
			case init_:
				return init(args);
			case computeWave_:
				return computeWave(args);
			case exchangeWave_:
				return (Object) exchangeWave(args);
			case collectWave_:
				return (Object) collectWave(args);
		}
		return null;
	}
	
	public Object init(Object args) {
		sizeX = getSize()[0];
		sizeY = getSize()[1]; // size is the base data members
		myX = getIndex()[0];
		myY = getIndex()[1]; // index is the base data members
		// reset the neighboring area information.
		neighbors[north] = neighbors[east] = neighbors[south] = neighbors[west] = 0.0;
		return null;
	}
	
	public Object computeWave(Object arg_time) {
		// retrieve the current simulation time
		time = ((Integer) arg_time).intValue();
		// move the previous return values to my neighbors[].
		if (getInMessages() != null) {
			for (int i = 0; i < 4; i++) {
				if (getInMessages()[i] != null) {
					neighbors[i] = ((Double) getInMessages()[i]);
				}
			}
		}
		if (myX == 0 || myX == sizeX - 1 || myY == 0 || myY == sizeY) {
			// this cell is on the edge of the Wave2D matrix
			if (time == 0) {
				wave[0] = 0.0; //current
			}
			if (time == 1) {
				wave[1] = 0.0; //previous
			} else if (time >= 2) {
				wave[2] = 0.0; //previous2
			}
		} else {
			// this cell is not on the edge
			if (time == 0) {
				// create an initial high tide in the central square area
				wave[0] = (sizeX * 0.4 <= myX && myX <= sizeX * 0.6 &&
						sizeY * 0.4 <= myY && myY <= sizeY * 0.6) ? 20.0 : 0.0;
				//start w/ wave[0]
				wave[1] = wave[2] = 0.0; // init wave[1] and wave[2] as 0.0
			} else if (time == 1) {
				// simulation at time 1
				wave[1] = wave[0] + c * c / 2.0 * dt * dt / (dd * dd) *
						(neighbors[north] + neighbors[east] + neighbors[south] +
								neighbors[west] - 4.0 * wave[0]); //wave[1] based on wave[0]
			} else if (time >= 2) {
				// simulation at time 2 and onwards
				wave[2] = 2.0 * wave[1] - wave[0] + c * c * dt * dt / (dd * dd) *
						(neighbors[north] + neighbors[east] + neighbors[south]
								+ neighbors[west] - 4.0 * wave[1]);
				//wave two based on wave[1] and wave[0]
				wave[0] = wave[1];
				wave[1] = wave[2];
				//shift wave[] measurements, prepare for a new wave[2]
			}
		}
		return null;
	}
	
	public Double exchangeWave(Object args) {
		return new Double(((time == 0) ? wave[0] : wave[1]));
		//wave one used after start
	}
	
	public Double collectWave(Object args) {
		return new Double(wave[2]);
	}
}