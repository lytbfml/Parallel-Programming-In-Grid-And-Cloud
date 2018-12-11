import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.log;

/**
 * Created by liyangde on Nov, 2018
 */
public class Randoms {
	
	public static final int IA = 16807;
	public static final int IM = Integer.MAX_VALUE;
	public static final double AM = 1.0 / IM;
	public static final int IQ = 127773;
	public static final int IR = 2836;
	public static final int NTAB = 32;
	public static final double NDIV = 1 + (IM - 1) / NTAB;
	public static final double EPS = 1.2e-7;
	public static final double RNMX = 1.0 - EPS;
	
	static long iy = 0;
	static long iv[] = new long[NTAB];
	static int iset = 0;
	
	static double gset;
	private long[] xpto;
	
	private Random rand;
	
	public Randoms(long x) {
		this.xpto = new long[2];
		this.xpto[0] = -x;
		rand = new Random();
	}

	public double Normal(double avg, double sigma) { return avg + sigma * gaussdev(this.xpto); }
	
	public double Uniforme() { return ran1(this.xpto);}
	
	public double sorte(int m) { return ThreadLocalRandom.current().nextDouble(-m, m + 1); }

	public double ran1(long[] idum) {
		int j;
		long k;
		
		double temp;
		if (idum[0] <= 0 || iy == 0) {           // Initialize.
			if (-(idum[0]) < 1) {
				idum[0] = 1;     // Be sure to prevent idum = 0.
			} else { idum[0] = -(idum[0]); }
			for (j = NTAB + 7; j >= 0; j--) {      // Load the shuffle table (after 8 warm-ups).
				k = (idum[0]) / IQ;
				idum[0] = IA * (idum[0] - k * IQ) - IR * k;
				if (idum[0] < 0) { idum[0] += IM; }
				if (j < NTAB) { iv[j] = idum[0]; }
			}
			iy = iv[0];
		}
		
		k = (idum[0]) / IQ;                     // Start here when not initializing.
		idum[0] =
				IA * (idum[0] - k * IQ) - IR * k;       // Compute idum=(IA*idum) % IM without over-
		if (idum[0] < 0) {
			idum[0] += IM;       // flows by Schrage's method.
		}
		j = (int) (iy / NDIV);                        //  Will be in the range 0..NTAB-1.
		iy = iv[j];                         // Output previously stored value and refill the
		iv[j] = idum[0];                    // shuffle table.
		if ((temp = AM * iy) > RNMX) {
			return RNMX;                   // Because users don't expect endpoint values.
		} else { return temp; }
	}
	
	public double gaussdev(long[] idum) {
		double fac, rsq, v1, v2;
		if (idum[0] < 0) {
			iset = 0;      //     Reinitialize.
		}
		if (iset == 0) {            //     We don't have an extra deviate handy, so
			do {
				v1 = 2.0 * ran1(idum) - 1.0;    // pick two uniform numbers in the square ex-
				v2 = 2.0 * ran1(idum) - 1.0;    // tending from -1 to +1 in each direction,
				rsq = v1 * v1 + v2 * v2;          // see if they are in the unit circle,
				
			} while (rsq >= 1.0 || rsq == 0.0);  // and if they are not, try again.
			fac = Math.sqrt(-2.0 * log(rsq) / rsq);
			// Now make the Box-Muller transformation to get two normal deviates.
			// Return one and save the other for next time.
			gset = v1 * fac;
			iset = 1;                 //  Set flag.
			return v2 * fac;
		} else {                    //   We have an extra deviate handy,
			iset = 0;                 //   so unset the flag,
			return gset;            //   and return it.
		}
	}
	
}
