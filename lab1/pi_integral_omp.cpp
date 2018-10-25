// Program to compute Pi using Integration

#include <iostream>
#include <stdio.h>
#include <math.h>
#include "Timer.h"   // for performance measurement
#include <omp.h>

using namespace std;

int main(int argc, char *argv[]) {
    int niter = 0;
    double PI25DT = 3.141592653589793238462643; // the actual PI
    double x = 0.0;
    int nThreads = 1;

    cout << "Enter the number of iterations used to estimate pi: ";
    cin >> niter;
    cout << "Enter the number of threads: ";
    cin >> nThreads;

    omp_set_num_threads(nThreads);
    
    Timer timer;
    timer.start();


    double h = 1.0 / (double) niter;
    double sum = 0.0;

#pragma omp parallel for default(shared) shared(h, niter) private(x) reduction( +:sum )
    for (int i = 1; i <= niter; i++) {
        // compute integral from 0.0 to 1.0
        x = h * ((double) i - 0.5);
        sum += (4.0 / (1.0 + x * x));
    }
    double pi = h * sum;

    cout << "elapsed time for pi = " << timer.lap() << endl;

    printf("# of trials = %d, estimate of pi is  %.16f, Error is %.16f\n",
           niter, pi, fabs(pi - PI25DT));

    return 0;
}
//elapsed time for pi = 55
//# of trials = 100, estimate of pi is  3.1416009869231254, Error is 0.0000083333333323
//elapsed time for pi = 1082
//# of trials = 100, estimate of pi is  3.1416009869231245, Error is 0.0000083333333314