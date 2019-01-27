#!/bin/bash 

opencv_createsamples -img ./positive_images/* -maxzangle 6.28 -maxyangle 0.9 -maxxangle 0.9 -w 50 -h 50 -vec pos-samples.vec
