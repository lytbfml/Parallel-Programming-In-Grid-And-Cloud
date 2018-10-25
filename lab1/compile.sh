#!/bin/sh
g++ pi_monte.cpp Timer.cpp -o pi_monte
g++ pi_monte_omp.cpp Timer.cpp -fopenmp -o pi_monte_omp

g++ pi_integral.cpp Timer.cpp -o pi_integral
g++ pi_integral_omp.cpp Timer.cpp -fopenmp -o pi_integral_omp


