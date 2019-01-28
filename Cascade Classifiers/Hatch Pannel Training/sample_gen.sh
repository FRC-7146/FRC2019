#!/bin/bash 

#opencv_createsamples -img ./positive_images/* -maxzangle 6.28 -maxyangle 1.0 -maxxangle 1.0 -w 80 -h 80 -vec pos-samples.vec -num 1000
opencv_createsamples -img ./positive_images/* -maxzangle 2.0 -maxyangle 0.2 -maxxangle 0.2 -w 150 -h 150 -vec pos-samples.vec -num 4000
