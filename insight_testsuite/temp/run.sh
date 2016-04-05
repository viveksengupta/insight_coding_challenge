# !/bin/bash

cd src
javac -cp "../libs/org.json.jar:../libs/commons-collections-2.0.jar" AverageDegree.java
echo "hello"
java -cp org.json.jar:commons-collections-2.0.jar:. AverageDegree
cd ..