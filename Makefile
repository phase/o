REGEXP := $(filter-out libregexp/test%.c,$(wildcard libregexp/*.c))

.PHONY: all
all:
	gcc o.c $(REGEXP) -o o -lm
ide:
	gcc o.c $(REGEXP) -DIDE -o oide -lm
clean:
	rm *.exe