/**
 * @author Yangxiao on 12/10/2018.
 */

import edu.uw.bothell.css.dsl.MASS.*;


public class Wave2DMass {
	
	public static void main(String[] args) throws Exception {
		
		MASS.init();
		// create a Wave2D array
		Places wave2D = new Places(1, Area.class.getName(), null, 100, 100);
		wave2D.callAll(Area.init_);
		// now go into a cyclic simulation
		for (int time = 0; time < 500; time++) {
			wave2D.callAll(Area.computeWave_, new Integer(time));
			wave2D.exchangeAll(1, Area.exchangeWave_);
		}
		MASS.finish();
	}
}