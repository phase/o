default: build
build:
	gcc o2.c -o o
ide:
	gcc o2.c -DIDE -o bin/o-ide