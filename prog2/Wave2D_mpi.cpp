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
    if (argc != 5) {
        cerr << "usage: Wave2D size max_time interval n_thread" << endl;
        return -1;
    }
    int size = atoi(argv[1]);
    int max_time = atoi(argv[2]);
    int interval = atoi(argv[3]);
    int nThread = atoi(argv[4]);
    int mpi_size;
    
    if (size < 100 || max_time < 3 || interval < 0 || nThread <= 0) {
        cerr << "usage: Wave2D size max_time interval" << endl;
        cerr << "       where size >= 100 && time >= 3 && interval >= 0 && mpi_size > 0" << endl;
        return -1;
    }
    
    MPI_Init(&argc, &argv); // start MPI
    MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
    MPI_Comm_size(MPI_COMM_WORLD, &mpi_size);
    
    omp_set_num_threads(nThreads);
    
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
//    if (my_rank == 0 && interval == 1) {
//        cout << "0" << endl;
//        for (int j = 0; j < size; j++) {
//            for (int i = 0; i < size; i++) {
//                cout << z[0][i][j] << " ";
//            }
//            cout << endl;
//        }
//        cout << endl;
//    }
    
    // time = 1
    for (int i = 1; i < size - 1; i++) {
        for (int j = 1; j < size - 1; j++) {
            z[1][i][j] = z[0][i][j] + (pow(c, 2) / 2) * pow(dt / dd, 2) * (z[0][i + 1][j] +
                    z[0][i - 1][j] + z[0][i][j + 1] + z[0][i][j - 1] - (4.0 * z[0][i][j]));
        }
    }
    
    //print every step include time = 1
//    if (my_rank == 0 && interval == 1) {
//        cout << 1 << endl;
//        for (int j = 0; j < size; j++) {
//            for (int i = 0; i < size; i++) {
//                cout << z[1][i][j] << " ";
//            }
//            cout << endl;
//        }
//        cout << endl;
//    }
    
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
        
        
        if (my_rank == 0) {
            MPI_Send(*(*(z + time_1) + stripe * (my_rank + 1) - 1), size, MPI_DOUBLE, my_rank +
                    1, 0, MPI_COMM_WORLD);
//            cout<< my_rank << ":"<< t <<"send over"<<endl;
            MPI_Status status;
            MPI_Recv(*(*(z + time_1) + stripe), size, MPI_DOUBLE, my_rank + 1, 0, MPI_COMM_WORLD,
                    &status);
//            cout<< my_rank << ":"<< t <<"recv over"<<endl;
            
        } else if (my_rank == mpi_size - 1) {
            MPI_Send(*(*(z + time_1) + stripe * my_rank), size, MPI_DOUBLE, my_rank -
                    1, 0, MPI_COMM_WORLD);
//            cout<< my_rank << ":"<< t <<"send over"<<endl;
            MPI_Status status;
            MPI_Recv(*(*(z + time_1) + stripe * my_rank - 1), size, MPI_DOUBLE, my_rank - 1, 0,
                    MPI_COMM_WORLD, &status);
//            cout<< my_rank << ":"<< t <<"recv over"<<endl;
        } else {
            MPI_Send(*(*(z + time_1) + stripe * my_rank), size, MPI_DOUBLE, my_rank - 1, 0,
                    MPI_COMM_WORLD);
//            cout<< my_rank << ":"<< t <<"send over"<<endl;
            MPI_Send(*(*(z + time_1) + stripe * (my_rank + 1) - 1), size, MPI_DOUBLE, my_rank + 1,
                    0, MPI_COMM_WORLD);
//            cout<< my_rank << ":"<< t <<"send over"<<endl;
            
            MPI_Status status;
            MPI_Recv(*(*(z + time_1) + stripe * my_rank - 1), size, MPI_DOUBLE, my_rank - 1, 0,
                    MPI_COMM_WORLD, &status);
//            cout<< my_rank << ":"<< t<<"recv over"<<endl;
            MPI_Recv(*(*(z + time_1) + stripe * (my_rank + 1)), size, MPI_DOUBLE, my_rank + 1, 0,
                    MPI_COMM_WORLD, &status);
//            cout<< my_rank << ":"<< t<<"recv over"<<endl;
        }
        
        for (int i = my_rank * stripe; i < (my_rank + 1) * stripe; i++) {
            if (i == 0 || i == size - 1) {
                continue;
            }
            for (int j = 1; j < size - 1; j++) {
                
                z[time][i][j] =
                        2.0 * z[time_1][i][j] - z[time_2][i][j] + (pow(c, 2) * pow(dt / dd, 2)
                                * (z[time_1][i + 1][j] + z[time_1][i - 1][j] + z[time_1][i][j + 1] +
                                z[time_1][i][j - 1] - (4.0 * z[time_1][i][j])));
            }
        }
        
        
        if (interval != 0 && t % interval == 0) {
            if (my_rank == 0) {
                for (int rank = 1; rank < mpi_size; ++rank) {
                    MPI_Status status;
                    MPI_Recv(*(*(z + time) + rank * stripe), stripe * size, MPI_DOUBLE, rank, 0,
                            MPI_COMM_WORLD, &status);
//                    cout<<"Aggregate:" << t <<endl;
                }
    
                cout << t << endl;
                for (int j = 0; j < size; j++) {
                    for (int i = 0; i < size; i++) {
                        cout << z[time][i][j] << " ";
                    }
                    cout << endl;
                }
                cout << endl;
                
            } else {
                MPI_Send(*(*(z + time) + my_rank * stripe), stripe * size, MPI_DOUBLE, 0, 0,
                        MPI_COMM_WORLD);
//                cout<< my_rank << ", " <<  t << " send to 0"<<endl;
            }
            
        }
        
    } // end of simulation
    
    MPI_Finalize(); // shut down MPI
    
    // finish the timer
    cerr << "Elapsed time = " << time.lap() << endl;
    
    return 0;
}