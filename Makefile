CC=gcc

o2: o2.c
	$(CC) -o $@ $< -lm

tst : o2.c
	$(CC) -o $@ $< -lm