# Get files from libregexp
REGEXP := $(filter-out libregexp/test%.c,$(wildcard libregexp/*.c))

.DEFAULT_GOAL := all

# Build a shared library for bindings
o.o:
	@mkdir -p bin
	gcc -c -fPIC o.c $(REGEXP)
	mv *.o bin/
shared: o.o
	gcc -shared -o o.so bin/*.o

all:
	gcc o.c $(REGEXP) -o o -lm

# Pass -DIDE flag and -O2
ide:
	gcc o.c $(REGEXP) -DIDE -O2 -o oide -lm

test:
	gcc o.c $(REGEXP) -DUTEST -o otest -lm

clean:
	rm *.exe
