cd src/xyz/jadonfowler/o
javac OCBindings.java
echo "Generated Class File"
cd ../../../../
mv src/xyz/jadonfowler/o/OCBindings.class ./
javah -classpath src/ xyz.jadonfowler.o.OCBindings
echo "Generated Header"
rm *.class