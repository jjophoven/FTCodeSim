package org.codeblooded.ftcodesim.hardware.devices;

import org.codeblooded.ftcodesim.hardware.SimHardwareMap;

public interface SimHardwareMechanism {
    void update(double deltaTime);
    default void registerDevices(SimHardwareMap hardwareMap) {}
}