REGEXP := $(filter-out libregexp/test%.c,$(wildcard libregexp/*.c))

default: build
build:
	gcc o.c $(REGEXP) -o o -lm
ide:
	gcc o.c $(REGEXP) -DIDE -o oide -lm
