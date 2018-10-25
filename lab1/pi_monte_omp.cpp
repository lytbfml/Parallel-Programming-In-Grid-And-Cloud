// Program to compute Pi using Monte Carlo methods
// Original code availabe from Dartmath through Internet
// Code modified to compute all four quadrants

#include "Timer.h"    // for performance measurement

#include <iostream>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <string.h>
#include <omp.h>

#define SEED 35791246

using namespace std;

int main(int argc, char *argv[]) {
    int niter = 0;
    double PI25DT = 3.141592653589793238462643; // the actual PI
    int nThreads = 1;
    double x, y;   // x-y coordinates in each quadrant
    int count = 0; // # of points in each quadrant of unit circle
    double radius;
    double pi = 0.0;

    cout << "Enter the number of iterations used to estimate pi: ";
    cin >> niter;
    cout << "Enter the number of threads: ";
    cin >> nThreads;

    omp_set_num_threads(nThreads);

    Timer timer;
    timer.start();

    srand(SEED);  // initialize random numbers

#pragma omp parallel for default(none) shared(niter) private(x, y, radius, count) reduction( +:pi )
    for (int quad = 0; quad < 4; quad++) {
        // for each quadrant
        count = 0;
        for (int i = 0; i < niter; i++) {
            x = (double) rand() / RAND_MAX;
            y = (double) rand() / RAND_MAX;
            radius = x * x + y * y;
            if (radius <= 1) count++;
        }
        pi += (double) count / niter;
    }

    cout << "elapsed time for pi = " << timer.lap() << endl;

    printf("# of trials = %d, estimate of pi is  %.16f, Error is %.16f\n",
           niter, pi, fabs(pi - PI25DT));

    return 0;
}
//# of trials = 1000000000, estimate of pi is  3.1416131730000001, Error is 0.0000205194102070
//# of trials = 1000000000, estimate of pi is  3.1416418490000000, Error is 0.0000491954102069
//# of trials = 1000000000, estimate of pi is  3.1415967750000000, Error is 0.0000041214102069