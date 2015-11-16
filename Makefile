default: build
build:
	gcc o.c -o o -lm
ide:
	gcc o.c -DIDE -o oide -lm