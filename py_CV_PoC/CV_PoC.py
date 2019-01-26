import numpy as np
import argparse
import time
import cv2

def PolygonArea(corners):
    n = len(corners) # of corners
    area = 0.0
    for i in range(n):
        j = (i + 1) % n
        area += corners[i][0] * corners[j][1]
        area -= corners[j][0] * corners[i][1]
    area = abs(area) / 2.0
    return area

# Hue from http://color.yafla.com/
# [360,100,100]
green_low=np.array([49,50,60])
green_high=np.array([100,100,100])

camera = cv2.VideoCapture(1)
camera.set(cv2.CAP_PROP_AUTO_EXPOSURE, True)
print(camera.isOpened())

while True:
    (grabbed, frame) = camera.read(cv2.COLOR_RGB2HSV)
    cv2.imshow('original', frame)

    # Remove noise
    frame = cv2.GaussianBlur(frame, (3, 3), 0)
    frame = cv2.bilateralFilter(frame, 9, 75, 75)

    # Detect yellow specific area
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    mask = cv2.inRange(hsv, green_low, green_high)
    res = cv2.bitwise_and(frame, frame, mask=mask)
    cv2.imshow('mask', mask)

    (img, cnts, _) = cv2.findContours(mask.copy(), cv2.RETR_LIST,
            cv2.CHAIN_APPROX_SIMPLE)
    cv2.drawContours(frame, cnts, -1, 255, 3)
    if len(cnts) >= 2:
        cnts = sorted(cnts, key=cv2.contourArea, reverse=True)
        rects=[]
        rects.append(np.int32(cv2.boxPoints(cv2.minAreaRect(cnts[0]))))
        rects.append(np.int32(cv2.boxPoints(cv2.minAreaRect(cnts[1]))))
        # area_diff= abs(PolygonArea(rects[0]) - PolygonArea(rects[1]))/PolygonArea(rects[0])
        area_diff= abs(1-PolygonArea(rects[0])/PolygonArea(rects[1]))
        xdiff=np.sum(np.abs(rects[0][:,1]-rects[1][:,1]))
        area_total=PolygonArea(rects[0])+PolygonArea(rects[1])
        print(area_diff)
        print(xdiff)
        print(area_total)
        print('----')

        if(area_diff<1.3 and xdiff<350 and area_total>2000):
            # Shape: [4,2]
            r = ((rects[0]+rects[1])/2).astype(np.int32)
            frame = cv2.drawContours(frame, [r], -1, (256, 50, 200), 4)
        # Draw a rectangular frame around the detected object
        frame = cv2.drawContours(frame, rects, -1, (100, 100, 256), 4)

    cv2.imshow('classified', frame)
    time.sleep(0.025)
    if cv2.waitKey(1) & 0xFF == ord("q"):
        break
camera.release()
