package com.acmerobotics.dashboard;

import org.codeblooded.ftcodesim.hardware.devices.SimTelemetry;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class FtcDashboard {
    private static final FtcDashboard instance = new FtcDashboard();

    private final Telemetry telemetry = new SimTelemetry();

    public static FtcDashboard getInstance() {
        return instance;
    }

    public Telemetry getTelemetry() {
        return telemetry;
    }
}