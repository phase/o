rm *.class
cd src/xyz/jadonfowler/o
rm *.class
javac OCBindings.java
cd ../../../../
mv src/xyz/jadonfowler/o/OCBindings.class ./
javah -classpath src/ xyz.jadonfowler.o.OCBindings