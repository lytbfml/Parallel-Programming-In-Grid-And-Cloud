#!/bin/sh

g++ initialize.cpp -o initialize
g++ -c EvalXOverMutate.cpp -fopenmp
g++ -c Timer.cpp
g++ Tsp.cpp Timer.o EvalXOverMutate.o -fopenmp -o Tsp




