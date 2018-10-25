//
// Created by yangxiao on 10/8/2018.
//
#include <iostream>  // cout
#include <fstream>   // ifstream
#include <string.h>  // strncpy
#include <stdlib.h>  // rand
#include <math.h>    // sqrt, pow
#include <time.h>
#include <omp.h>
#include <bits/stdc++.h>
#include "Trip.h"

using namespace std;


float getDis(char a, char b, int coordinates[CITIES][2]);

float getDisFromIt(char str[CITIES], float disMax[CITIES][CITIES + 1]);

char *getChildB(char childA[CITIES]);

int getIndex(char a, char source[CITIES]);

int getAscii(char source);

const char cit[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

/**
 * Compare tow trip based their fitness, uses as comparator for sorting
 * @param t1 trip 1
 * @param t2 trip 2
 * @return if t1 < t2
 */
bool compareTrip(Trip t1, Trip t2) {
    return (t1.fitness < t2.fitness);
}

/**
 * evaluates the distance of each trip and sorts out all the trips in the shortest-first order
 * @param trip trips to evaluates
 * @param coordinates coordinates of all cities
 * @param first if it's first generation
 */
void evaluate(Trip trip[CHROMOSOMES], int coordinates[CITIES][2]) {
    //Parallelization
#pragma omp parallel for
    for (int i = 0; i < CHROMOSOMES; ++i) {
        char temp[CITIES];
        strncpy(temp, trip[i].itinerary, CITIES);
        int indexIn = (temp[0] >= 'A') ? temp[0] - 'A' : temp[0] - '0' + 26;
        double dis = hypot(coordinates[indexIn][0], coordinates[indexIn][1]);
        for (int j = 1; j < CITIES; ++j) {
            int index = (temp[j] >= 'A') ? temp[j] - 'A' : temp[j] - '0' + 26;
            dis += hypot((coordinates[indexIn][0] - coordinates[index][0]),
                         (coordinates[indexIn][1] - coordinates[index][1]));
            indexIn = index;
        }
        trip[i].fitness = (float) dis;
    }
    sort(trip, trip + CHROMOSOMES, compareTrip);
}

/**
 * generates 25,000 off-springs from the parents, calculate distance based on their coordinates
 * @param parents
 * @param offsprings
 * @param coordinates
 */
void crossover(Trip parents[TOP_X], Trip offsprings[TOP_X], int coordinates[CITIES][2]) {
#pragma omp parallel for
    for (int i = 0; i < TOP_X; i += 2) {
        int selected[100] = {0};
        char child1[CITIES + 1];
        char child2[CITIES + 1];
        char *parent1 = parents[i].itinerary;
        char *parent2 = parents[i + 1].itinerary;
        child1[0] = parent1[0];
        selected[child1[0]] = 1;
        
        for (int j = 0; j < (CITIES - 1); j++) {
            int indexInA = getIndex(child1[j], parent1) + 1;
            int indexInB = getIndex(child1[j], parent2) + 1;
            
            if (indexInA == -1 || indexInB == -1) {
                printf("WHat?\n");
            }
            
            if (indexInA >= CITIES) {
                indexInA = 0;
            }
            if (indexInB >= CITIES) {
                indexInB = 0;
            }
            
            float disA = INT_MAX;
            float disB = INT_MAX;
            
            if (selected[parent1[indexInA]] == 0) {
                disA = getDis(child1[j], parent1[indexInA], coordinates);
            }
            if (selected[parent2[indexInB]] == 0) {
                disB = getDis(child1[j], parent2[indexInB], coordinates);
            }
            
            if (disA == INT_MAX && disB == INT_MAX) {
                int r = rand() % CITIES;
                int found = 1;
                while (found) {
                    if (selected[parent1[r]] != 1) {
                        found = 0;
                        child1[j + 1] = parent1[r];
                    }
                    r = ((r + 1) >= CITIES) ? 0 : (r + 1);
                }
            } else {
                child1[j + 1] = (disA <= disB) ? parent1[indexInA] : parent2[indexInB];
            }
            
            selected[child1[j + 1]] = 1;
        }
        
        strcpy(child2, getChildB(child1));
        strncpy(offsprings[i].itinerary, child1, CITIES);
        strncpy(offsprings[i + 1].itinerary, child2, CITIES);
    }
}


/**
 * Improved version, when mutate each city, perform mutation if the new one has shorter distance,
 * otherwise, do nothing
 *
 * randomly chooses two distinct cities (or genes) in each trip (or chromosome) with a
 * given probability, and swaps them
 * @param offsprings
 * @param disMax
 */
void mutateB(Trip offsprings[TOP_X], float disMax[CITIES][CITIES + 1]) {
    for (int i = 0; i < TOP_X; ++i) {
        int rate = rand() % 100;
        if (rate < MUTATE_RATE) {
            int r1 = rand() % CITIES;
            int r2 = rand() % CITIES;
            char temp[CITIES + 1];
            strncpy(temp, offsprings[i].itinerary, CITIES);
            temp[r1] = offsprings[i].itinerary[r2];
            temp[r2] = offsprings[i].itinerary[r1];
            float d1 = getDisFromIt(offsprings[i].itinerary, disMax);
            float d2 = getDisFromIt(temp, disMax);
            //check if the new one has shorter distacne
            if (d1 > d2) {
                strncpy(offsprings[i].itinerary, temp, CITIES);
            }
        }
    }
}

/**
 * Helper function that calculate distance between two cities
 */
float getDis(char a, char b, int coordinates[CITIES][2]) {
    int indexA = (a >= 'A') ? a - 'A' : a - '0' + 26;
    int indexB = (b >= 'A') ? b - 'A' : b - '0' + 26;
    float dis = (float) hypot((coordinates[indexA][0] - coordinates[indexB][0]),
                              (coordinates[indexA][1] - coordinates[indexB][1]));
    return dis;
}

/**
 * Get distacne of the provided route from distance matrix
 */
float getDisFromIt(char str[CITIES], float disMax[CITIES][CITIES + 1]) {
    int indexIn = (str[0] >= 'A') ? str[0] - 'A' : str[0] - '0' + 26;
    double dis = disMax[indexIn][CITIES];
    for (int j = 1; j < CITIES; ++j) {
        int index = (str[j] >= 'A') ? str[j] - 'A' : str[j] - '0' + 26;
        dis += disMax[indexIn][index];
        
        indexIn = index;
    }
    return dis;
}

/**
 * Get the position of the target character
 * @param a target character
 * @param source source string
 * @return index of target char, -1 if not found
 */
int getIndex(char a, char source[CITIES]) {
    for (int i = 0; i < CITIES; ++i) {
        if (a == source[i]) return i;
    }
    return -1;
}

/**
 * get ascii representation of target char, and map it to its index based on
 * ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789
 */
int getAscii(char source) {
    return (source >= 'A') ? source - 'A' : source - '0' + 26;
}

/**
 * generate childA's complement
 */
char *getChildB(char childA[CITIES]) {
    char *childB = (char *) malloc(sizeof(char) * CITIES);
    for (int i = 0; i < CITIES; ++i) {
        childB[i] = cit[(CITIES - getAscii(childA[i]) - 1)];
    }
    return childB;
}


/**
 * Improved version with distance matrix, avoid calculating distance
 * generates 25,000 off-springs from the parents, calculate distance based on their coordinates
 * @param parents
 * @param offsprings
 * @param disMax
 */
void crossoverB(Trip parents[TOP_X], Trip offsprings[TOP_X], float disMax[CITIES][CITIES + 1]) {
#pragma omp parallel for default(none) shared(parents, disMax, offsprings)
    for (int i = 0; i < TOP_X; i += 2) {
        int selected[100] = {0};
        char child1[CITIES + 1];
        char child2[CITIES + 1];
        char *parent1 = parents[i].itinerary;
        char *parent2 = parents[i + 1].itinerary;
        child1[0] = parent1[0];
        selected[child1[0]] = 1;
        
        for (int j = 0; j < (CITIES - 1); j++) {
            int indexInA = getIndex(child1[j], parent1) + 1;
            int indexInB = getIndex(child1[j], parent2) + 1;
            
            if (indexInA == -1 || indexInB == -1) {
                printf("WHat?\n");
            }
            
            if (indexInA >= CITIES) {
                indexInA = 0;
            }
            if (indexInB >= CITIES) {
                indexInB = 0;
            }
            
            float disA = INT_MAX;
            float disB = INT_MAX;
            
            if (selected[parent1[indexInA]] == 0) {
                int indA = getAscii(child1[j]);
                int indB = getAscii(parent1[indexInA]);
                disA = disMax[indA][indB];
            }
            if (selected[parent2[indexInB]] == 0) {
                int indA = getAscii(child1[j]);
                int indB = getAscii(parent2[indexInB]);
                disB = disMax[indA][indB];
            }
            
            if (disA == INT_MAX && disB == INT_MAX) {
                int r = rand() % CITIES;
                int found = 0;
                while (!found) {
                    if (selected[parent1[r]] != 1) {
                        found = 1;
                        child1[j + 1] = parent1[r];
                    }
                    r = ((r + 1) >= CITIES) ? 0 : (r + 1);
                }
            } else {
                child1[j + 1] = (disA <= disB) ? parent1[indexInA] : parent2[indexInB];
            }
            
            selected[child1[j + 1]] = 1;
        }
        
        strcpy(child2, getChildB(child1));
        strncpy(offsprings[i].itinerary, child1, CITIES);
        strncpy(offsprings[i + 1].itinerary, child2, CITIES);
    }
}

/**
 * randomly chooses two distinct cities (or genes) in each trip (or chromosome) with a
 * given probability, and swaps them
 * @param offsprings
 */
void mutate(Trip offsprings[TOP_X]) {
    for (int i = 0; i < TOP_X; ++i) {
        int rate = rand() % 100;
        if (rate < MUTATE_RATE) {
            int r1 = rand() % CITIES;
            int r2 = rand() % CITIES;
            char x1 = offsprings[i].itinerary[r1];
            char x2 = offsprings[i].itinerary[r2];
            offsprings[i].itinerary[r1] = x2;
            offsprings[i].itinerary[r2] = x1;
        }
    }
}


/**
 * Improved verison, uses distance matrix to avoid calculating distance
 * Evaluates the distance of each trip and sorts out all the trips in the shortest-first order
 * @param trip trips to evaluates
 * @param disMax maxtrix that contain all the distance
 * @param first if it's first generation
 */
void evaluateB(Trip trip[CHROMOSOMES], float disMax[CITIES][CITIES + 1], bool first) {
    int start = 0;
    if (!first) {
        start = TOP_X;
    }

#pragma omp parallel for default(none) firstprivate(start) shared(trip, disMax)
    for (int i = start; i < CHROMOSOMES; ++i) {
        char temp[CITIES];
        strncpy(temp, trip[i].itinerary, CITIES);
        int indexIn = (temp[0] >= 'A') ? temp[0] - 'A' : temp[0] - '0' + 26;
        double dis = disMax[indexIn][CITIES];
        for (int j = 1; j < CITIES; ++j) {
            int index = (temp[j] >= 'A') ? temp[j] - 'A' : temp[j] - '0' + 26;
            dis += disMax[indexIn][index];
            
            indexIn = index;
        }
        trip[i].fitness = (float) dis;
//        printf("Thread %d executes loop iteration %d\n", omp_get_thread_num(), i);
    }
    sort(trip, trip + CHROMOSOMES, compareTrip);
}