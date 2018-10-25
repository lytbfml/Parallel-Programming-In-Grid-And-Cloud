#!/bin/sh
mpic++ greetings.cpp -o greetings
g++ matrix.cpp Timer.cpp -o matrix
mpic++ matrix_mpi.cpp Timer.cpp -o matrix_mpi



