package org.jjophoven.fakehardware.devices;

import org.jjophoven.fit.MotorModel;

public class FakeMotorConfig {
    public String name;
    public MotorModel motorModel;
    public double[] modelCoefficients;
    public double[] zeroPowerBrakeCoefficients;
    public double staticVelocityRegion;
    public double staticFriction;
    public FakeVoltageSensor voltageSensor;

    public FakeMotorConfig(String name, MotorModel motorModel, double[] modelCoefficients, double[] zeroPowerBrakeCoefficients, double staticVelocityRegion, double staticFriction, FakeVoltageSensor voltageSensor) {
        this.name = name;
        this.motorModel = motorModel;
        this.modelCoefficients = modelCoefficients;
        this.zeroPowerBrakeCoefficients = zeroPowerBrakeCoefficients;
        this.staticVelocityRegion = staticVelocityRegion;
        this.staticFriction = staticFriction;
        this.voltageSensor = voltageSensor;
    }
}
