#!/bin/sh

javac -cp jars/spark-core_2.11-2.3.1.jar:jars/spark-sql_2.11-2.3.1.jar:jars/scala-library-2.11.8.jar:google-collections-1.0.jar $1.java
jar -cvf $1.jar $1*.class
