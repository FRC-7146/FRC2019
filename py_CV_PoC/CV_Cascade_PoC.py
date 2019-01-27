import numpy as np
import argparse
import time
import cv2

face_cascade = cv2.CascadeClassifier('./haarcascade_frontalface_default.xml')
eye_cascade = cv2.CascadeClassifier('./haarcascade_eye.xml')

camera = cv2.VideoCapture(1)
camera.set(cv2.CAP_PROP_AUTO_EXPOSURE, True)
camera.set(cv2.CAP_PROP_AUTO_EXPOSURE, False)
print(camera.isOpened())

while True:
    (grabbed, frame) = camera.read(cv2.COLOR_RGB2HSV)
    cv2.imshow('original', frame)
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, 1.3, 5)
    for (x, y, w, h) in faces:
        cv2.rectangle(frame, (x, y), (x + w, y + h), (255, 0, 0), 2)
        roi_gray = gray[y:y + h, x:x + w]
        roi_color = frame[y:y + h, x:x + w]
        eyes = eye_cascade.detectMultiScale(roi_gray)
        for (ex, ey, ew, eh) in eyes:
            cv2.rectangle(roi_color, (ex, ey), (ex + ew, ey + eh), (0, 255, 0),
                          2)

    cv2.imshow('img', frame)
    time.sleep(0.025)
    if key == ord("q"):
        break
camera.release()
