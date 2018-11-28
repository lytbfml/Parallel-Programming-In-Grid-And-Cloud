#!/bin/sh

spark-submit --class $1 --master local $1.jar $2 $3 $4

