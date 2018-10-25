#include <iostream>
#include "Timer.h"
#include <stdlib.h>   // atoi
#include <math.h>
#include <mpi.h>
#include <stdio.h>

int default_size = 100;  // the default system size
int defaultCellWidth = 8;
double c = 1.0;      // wave speed
double dt = 0.1;     // time quantum
double dd = 2.0;     // change in system

using namespace std;

int main(int argc, char *argv[]) {
    int my_rank = 0;            // used by MPI
               // used by MPI
    
    
    
    // verify arguments
    if (argc != 4) {
        cerr << "usage: Wave2D size max_time interval mpi_size" << endl;
        return -1;
    }
    int size = atoi(argv[1]);
    int max_time = atoi(argv[2]);
    int interval = atoi(argv[3]);
    int mpi_size = 1;
    
    if (size < 100 || max_time < 3 || interval < 0 || mpi_size <= 0) {
        cerr << "usage: Wave2D size max_time interval" << endl;
        cerr << "       where size >= 100 && time >= 3 && interval >= 0 && mpi_size > 0" << endl;
        return -1;
    }
    
    MPI_Init(&argc, &argv); // start MPI
    MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
    MPI_Comm_size(MPI_COMM_WORLD, &mpi_size);
    
    
    // create a simulation space
    double z[3][size][size];
    for (int p = 0; p < 3; p++)
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                z[p][i][j] = 0.0; // no wave
    
    // start a timer
    Timer time;
    time.start();
    
    // time = 0;
    // initialize the simulation space: calculate z[0][][]
    int weight = size / default_size;
    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            if (i > 40 * weight && i < 60 * weight &&
                    j > 40 * weight && j < 60 * weight) {
                z[0][i][j] = 20.0;
            } else {
                z[0][i][j] = 0.0;
            }
        }
    }
    
    //print every step include time = 0
    if (my_rank == 0 && interval == 1) {
        printf("0\n");
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                printf("%g ", z[0][i][j]);
            }
            printf("\n");
        }
        printf("\n");
    }
    
    // time = 1
    for (int i = 1; i < size - 1; i++) {
        for (int j = 1; j < size - 1; j++) {
            z[1][i][j] = z[0][i][j] + (pow(c, 2) / 2) * pow(dt / dd, 2) * (z[0][i + 1][j] +
                    z[0][i - 1][j] + z[0][i][j + 1] + z[0][i][j - 1] - (4.0 * z[0][i][j]));
        }
    }
    
    //print every step include time = 1
    if (my_rank == 0 && interval == 1) {
        printf("1\n");
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                if (abs(z[1][i][j] - 0.0) > 0.0) {
                    printf("%g ", z[1][i][j]);
                } else {
                    printf("0 ");
                }
            }
            printf("\n");
        }
        printf("\n");
    }
    
    int stripe = size / mpi_size;     // partitioned stripe
    
    // simulate wave diffusion from time = 2
    for (int t = 2; t < max_time; t++) {
        int time = t % 3;
        int time_1, time_2;
        if (time == 0) {
            time_1 = 2;
            time_2 = 1;
        } else if (time == 1) {
            time_1 = 0;
            time_2 = 2; // this
        } else {
            time_1 = 1;
            time_2 = 0;
        }
        
        double bound1[size];
        double bound2[size];
        
        for (int i = 0; i < size; ++i) {
            bound1[i] = z[time_1][stripe * my_rank][i];
            bound2[i] = z[time_1][stripe * (my_rank + 1) - 1][i];
        }
        if (my_rank == 0) {
            MPI_Send(bound2, size, MPI_DOUBLE, my_rank + 1, 0, MPI_COMM_WORLD);
            
            MPI_Status status;
            MPI_Recv(bound2, size, MPI_DOUBLE, my_rank + 1, 0, MPI_COMM_WORLD, &status);
        } else if (my_rank == 1) {
            MPI_Send(bound1, size, MPI_DOUBLE, my_rank - 1, 0, MPI_COMM_WORLD);
            MPI_Send(bound2, size, MPI_DOUBLE, my_rank + 1, 0, MPI_COMM_WORLD);
            
            MPI_Status status;
            MPI_Recv(bound1, size, MPI_DOUBLE, my_rank - 1, 0, MPI_COMM_WORLD, &status);
            MPI_Recv(bound2, size, MPI_DOUBLE, my_rank + 1, 0, MPI_COMM_WORLD, &status);
        } else if (my_rank == 2) {
            MPI_Send(bound1, size, MPI_DOUBLE, my_rank - 1, 0, MPI_COMM_WORLD);
            MPI_Send(bound2, size, MPI_DOUBLE, my_rank + 1, 0, MPI_COMM_WORLD);
            
            MPI_Status status;
            MPI_Recv(bound1, size, MPI_DOUBLE, my_rank - 1, 0, MPI_COMM_WORLD, &status);
            MPI_Recv(bound2, size, MPI_DOUBLE, my_rank + 1, 0, MPI_COMM_WORLD, &status);
        } else {
            MPI_Send(bound1, size, MPI_DOUBLE, my_rank - 1, 0, MPI_COMM_WORLD);
            
            MPI_Status status;
            MPI_Recv(bound1, size, MPI_DOUBLE, my_rank - 1, 0, MPI_COMM_WORLD, &status);
        }
        
        
        for (int i = 0; i < size; ++i) {
            if (my_rank != 0) {
                z[time_1][stripe * my_rank - 1][i] = bound1[i];
            }
            if (my_rank != (mpi_size - 1)) {
                z[time_1][stripe * (my_rank + 1)][i] = bound2[i];
            }
        }
        
        for (int i = 0; i < size; ++i) {
            
            z[time_1][stripe * my_rank - 1][i] = bound1[i];
            z[time_1][stripe * (my_rank + 1)][i] = bound2[i];
        }
        
        for (int i = my_rank * stripe; i < (my_rank + 1) * stripe; i++) {
            if (i == 0) {
                continue;
            }
            for (int j = 1; j < size - 1; j++) {
                
                z[time][i][j] =
                        2.0 * z[time_1][i][j] - z[time_2][i][j] + (pow(c, 2) * pow(dt / dd, 2)
                                * (z[time_1][i + 1][j] + z[time_1][i - 1][j] + z[time_1][i][j + 1] +
                                z[time_1][i][j - 1] - (4.0 * z[time_1][i][j])));
            }
        }
        
        if(my_rank != 0) {
        
        }
        
        if (my_rank == 0 && t % interval == 0) {
            printf("%d\n", t);
            for (int j = 0; j < size; j++) {
                for (int i = 0; i < size; i++) {
                    if (abs(z[time][i][j] - 0.0) > 0.0) {
                        printf("%g ", z[time][i][j]);
                    } else {
                        printf("0 ");
                    }
                }
                printf("\n");
            }
            printf("\n");
        }
    } // end of simulation
    
    
    // finish the timer
    cerr << "Elapsed time = " << time.lap() << endl;
    
    MPI_Finalize(); // shut down MPI
    
    return 0;
}