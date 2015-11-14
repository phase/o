default: build
build:
	gcc o.c -o o -lm
ide:
	mkdir -p bin
	gcc o.c -DIDE -o bin/o-ide -lm