//
// Created by lytbf on 11/6/2018.
//
#include <iostream>
#include "Timer.h"
#include <stdlib.h>
#include <math.h>
#include <mpi.h>
#include <stdio.h>
#include <omp.h>

using namespace std;

int size = 500; // simulation space
int cur = 0, next = 1;

void main(int argc, char *argv[]) {
    
    double p[2][size][size][size]; // air pressure, p[0][][][] = current, p[1][][] = next pressure
    double v[2][size][size][size]; // air velocity, v[0][][][] = current, v[1][][] = next velocity
    double t[2][size][size][size]; // air temperature, t[0][][][] = current, t[1][][] = next temp
    
    for (int time = 0; time < 500; time++) {
        // air pressure calculation
        // each p[next][i][j][k] needs its neighboring p with width 1 in j,j,k and v[cur][i][j][k].
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                for (int k = 0; k < size; k++) {
                    if (boundary(i, j, k, size))
                        p[next][i][j][k] = boundaryPressure(p[cur][i][j][k], size);
                    else
                        p[next][i][j][k] = nextPressure(p[cur][i + 1][j][k], p[cur][i - 1][j][k],
                                p[cur][i][j + 1][k], p[cur][i][j + 1][k],
                                p[cur][i][j][k + 1], p[cur][i][j][k - 1],
                                v[cur][i][j][k]);
                }
        // air velocity calculation
        // each v[next][i][j][k] needs its neighboring v with width 2 in i,j,k.
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                for (int k = 0; k < size; k++) {
                    if (boundary(i, j, k, size))
                        v[next][i][j][k] = boundaryVelocity(v[cur][i][j][k], size);
                    else
                        v[next][i][j][k] =
                                nextVelocity(v[cur][i][j][k], v[cur][i][j - 1][k],
                                        v[cur][i][j][k - 1],
                                        v[cur][i + 1][j][k], v[cur][i + 1][j - 1][k],
                                        v[cur][i + 1][j][k - 1],
                                        v[cur][i + 2][j][k], v[cur][i + 2][j - 1][k],
                                        v[cur][i + 2][j][k - 1],
                                        
                                        v[cur][i - 1][j][k],
                                        v[cur][i][j + 1][k], v[cur][i - 1][j + 1][k],
                                        v[cur][i][j + 1][k - 1],
                                        v[cur][i][j + 2][k], v[cur][i - 1][j + 2][k],
                                        v[cur][i][j + 2][k - 1],
                                        
                                        v[cur][i][j][k + 1], v[cur][i - 1][j][k + 1],
                                        v[cur][i][j - 1][k + 1],
                                        v[cur][i][j][k + 2], v[cur][i - 1][j][k + 2],
                                        v[cur][i][j - 2][k + 2]
                                );
                }
        
        // air temperature calculation
        // each t[next][i][j][k] needs its neighboring t with width 1 in i,j,k and v[next]
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                for (int k = 0; k < size; k++) {
                    if (boundary(i, j, k, size))
                        t[next][i][j][k] = boundaryTemperature(t[cur][i][j][k], size);
                    else
                        t[next][i][j][k] = nextTemperature(t[cur][i + 1][j][k], t[cur][i - 1][j][k],
                                t[cur][i][j + 1][k], t[cur][i][j + 1][k],
                                t[cur][i][j][k + 1], t[cur][i][j][k - 1],
                                v[next][i][j][k]);
                }
        
        
        // move p,v,t[next][size][size] to p,v,t[cur][size][size], changing index only
        int x = next;
        next = cur;
        cur = x;
    }
}


// checking if a given cubicle is on a boundary
bool boundary(int i, int j, int k, int size) {
    return (i == 0 || i == size - 2 || i == size - 1 ||
            j == 0 || j == size - 2 || j == size - 1 ||
            k == 0 || k == size - 2 || k == size - 1);
}

double boundaryPressure(double p, int size) {
    double pressure; // compute the next boundary pressure
    return pressure;
}

double boundaryVelocity(double v, int size) {
    double volocity; // compute the next boundary velocity
    return volocity;
}

double boundaryTemperature(double t, int size) {
    
    double temperature; // compute the next boundary temperature
    return temperature;
}

double nextPressure(double p1, double p2, double p3, double p4, double p5, double p6,
        double v1) { // note that the last argument is velocity
    double pressure; // compute the next pressure
    return pressure;
}


double nextVelocity(double v1, double v2, double v3, double v4, double v5, double v6,
        double v7, double v8, double v9, double v10, double v11, double v12,
        double v13, double v14, double v15, double v16, double v17, double v18,
        double v19, double v20, double v21, double v22) {
    double volocity; // compute the next velocity
    return volocity;
}

double nextTemperature(double t1, double t2, double t3, double t4, double t5, double t6,
        double v1) { // note that the last argument is velocity
    double temperature; // compute the next temperature
    return temperature;
}
