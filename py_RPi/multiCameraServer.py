#!/usr/bin/env python3
#----------------------------------------------------------------------------
# Copyright (c) 2018 FIRST. All Rights Reserved.
# Open Source Software - may be modified and shared by FRC teams. The code
# must be accompanied by the FIRST BSD license file in the root directory of
# the project.
#----------------------------------------------------------------------------

import json
import time
import sys

from cscore import CameraServer, VideoSource, UsbCamera, MjpegServer
from networktables import NetworkTablesInstance

configFile = "/boot/frc.json"


class CameraConfig:
    pass


team = None
server = False
json_instance = None
cameraConfigs = []

"""Report parse error."""


def parseError(str):
    print("config error in '" + configFile + "': " + str)
    # print("config error in '" + configFile + "': " + str, file=sys.stderr)


"""Read single camera configuration."""


def readCameraConfig(config):
    cam = CameraConfig()

    # name
    try:
        cam.name = config["name"]
    except KeyError:
        parseError("could not read camera name")
        return False

    # path
    try:
        cam.path = config["path"]
    except KeyError:
        parseError("camera '{}': could not read path".format(cam.name))
        return False

    # stream properties
    cam.streamConfig = config.get("stream")

    cam.config = config

    cameraConfigs.append(cam)
    return True


"""Read configuration file."""


def readConfig():
    global team
    global server

    # parse file
    try:
        with open(configFile, "rt") as f:
            j = json.load(f)
    except OSError as err:
        print("could not open '{}': {}".format(configFile, err))
        return False

    # top level must be an object
    if not isinstance(j, dict):
        parseError("must be JSON object")
        return False

    # team number
    try:
        team = j["team"]
    except KeyError:
        parseError("could not read team number")
        team = 7146
        # return False

    # ntmode (optional)
    if "ntmode" in j:
        str = j["ntmode"]
        if str.lower() == "client":
            server = False
        elif str.lower() == "server":
            server = True
        else:
            parseError("could not understand ntmode value '{}'".format(str))

    # cameras
    try:
        cameras = j["cameras"]
    except KeyError:
        parseError("could not read cameras")
        return False
    for camera in cameras:
        if not readCameraConfig(camera):
            return False
    json_instance = j
    return True


"""Start running the camera."""


def startCamera(config):
    print("Starting camera '{}' on {}".format(config.name, config.path))
    inst = CameraServer.getInstance()
    camera = UsbCamera(config.name, config.path)
    server = inst.startAutomaticCapture(camera=camera, return_server=True)

    camera.setConfigJson(json.dumps(config.config))
    camera.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen)

    if config.streamConfig is not None:
        server.setConfigJson(json.dumps(config.streamConfig))

    return camera


if __name__ == "__main__":
    if len(sys.argv) >= 2:
        configFile = sys.argv[1]

    # read configuration
    if not readConfig():
        sys.exit(1)

    # start NetworkTables
    ntinst = NetworkTablesInstance.getDefault()
    if server:
        print("Setting up NetworkTables server")
        ntinst.startServer()
    else:
        print("Setting up NetworkTables client for team {}".format(team))
        ntinst.startClientTeam(team)

    # start cameras
    cameras = []
    for cameraConfig in cameraConfigs:
        cameras.append(startCamera(cameraConfig))

    SmartDashBoard = ntinst.getTable('SmartDashboard')
    SmartDashBoard.putBoolean('Pi Running', True)

    resolution=(800,600)
    import cv2
    import numpy as np
    camera = None
    if(cameras.__len__() > 0):
        camera = cameras[0]
    cvSink=CameraServer.getInstance().getVideo()
    piSrc=CameraServer.getInstance().putVideo('Pi out',resolution[0],resolution[1])
    frame=np.zeros([resolution[0],resolution[1],3])
    try:
        while True:
            if(cameras.__len__() > 0):
                camera = cameras[0]
                print(camera)
                if(0==cvSink.grabFrame(frame)):
                    print('error grabbing frame')
                else:
                    cv2.cvtColor(frame,cv2.COLOR_BGR2GRAY)
                    piSrc.putFrame(frame)
            else:
                time.sleep(10)
    except Exception as e:
        print('Error in CV: {}'.format(e))
        SmartDashBoard.putBoolean('Pi Running', False)
