# Get files from libregexp
REGEXP := $(filter-out libregexp/test%.c,$(wildcard libregexp/*.c))

.PHONY: all

# Build a shared library for bindings
o.o:
	gcc -c -fPIC -o o.o o.c
shared: o.o
	gcc -shared -o o.so o.o

all:
	gcc o.c $(REGEXP) -o o -lm

# Pass -DIDE flag and -O2
ide:
	gcc o.c $(REGEXP) -DIDE -O2 -o oide -lm


clean:
	rm *.exe
