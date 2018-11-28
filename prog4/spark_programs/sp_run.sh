#!/bin/sh

spark-submit --class ShortestPath --master $1 --total-executor-cores $2 ShortestPath.jar ./graph.txt 0 1500 > output


