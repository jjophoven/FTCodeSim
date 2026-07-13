package org.codeblooded.ftcodesim.simulator;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.codeblooded.ftcodesim.hardware.SimHardwareMap;
import org.codeblooded.ftcodesim.hardware.devices.SimTelemetry;
import org.psilynx.psikit.core.Logger;


public class OpModeLifecycle {
    public volatile boolean isStarted = false;
    public volatile boolean isStopped = false;
    OpMode opMode;
    SimHardwareMap simHardwareMap;
    SimTelemetry telemetry;
    long loopTimeMs;
    volatile byte[] latestGamepad1Data = new Gamepad().toByteArray();
    volatile byte[] latestGamepad2Data = new Gamepad().toByteArray();

    public OpModeLifecycle(OpMode opMode, SimTelemetry telemetry, SimHardwareMap simHardwareMap, long loopTimeMs) {
        this.opMode = opMode;
        this.simHardwareMap = simHardwareMap;
        this.loopTimeMs = loopTimeMs;
        this.telemetry = telemetry;
    }

    public void runOpMode() throws InterruptedException {
        opMode.telemetry = telemetry;
        opMode.hardwareMap = simHardwareMap;
        opMode.gamepad1 = new Gamepad();
        opMode.gamepad2 = new Gamepad();

        SimFtcLogger ftcLog = new SimFtcLogger();
        ftcLog.start(opMode, 5800, "", true, "sim-logs");
        Logger.setSimulation(true);

        long start = System.nanoTime();
        Logger.setTimeSource(() -> (System.nanoTime() - start) * 1e-9);

        opMode.init(); // TODO wrap this with internals

        while (!isStarted && !isStopped) {
            wrap(opMode::init_loop, opMode, ftcLog);
        }

        opMode.start();

        while (!isStopped) {
            wrap(opMode::loop, opMode, ftcLog);
        }

        opMode.stop();

        Logger.end();
    }

    public void wrap(Runnable runnable, OpMode opMode, SimFtcLogger ftcLog) throws InterruptedException {
        long loopStart = System.nanoTime();

        // FTC SDK's internalPreUserCode
        opMode.time = opMode.getRuntime();
        opMode.gamepad1.fromByteArray(latestGamepad1Data);
        opMode.gamepad2.fromByteArray(latestGamepad2Data);

        simHardwareMap.update();

        long logStart = System.nanoTime();

        ftcLog.logOncePerLoop(opMode, isStarted, isStopped);
        Logger.periodicBeforeUser();

        long userCodeStart = System.nanoTime();
        runnable.run();
        long userCodeEnd = System.nanoTime();

        Logger.periodicAfterUser(0,0);

        long logEnd = System.nanoTime();

        // FTC SDK's internalPostUserCode
        opMode.telemetry.update();

        long simEnd = System.nanoTime();

        Thread.sleep(loopTimeMs);

        long loopEnd = System.nanoTime();
        long userCodeTime = userCodeEnd - userCodeStart;
        Logger.recordOutput("OpMode/noSleepMs", (simEnd - loopStart) * 1e-6);
        Logger.recordOutput("OpMode/totalLoopTimeMs", (loopEnd - loopStart) * 1e-6);
        Logger.recordOutput("OpMode/userCodeMs", userCodeTime * 1e-6);
        Logger.recordOutput("OpMode/simHardwareMs", (logStart - loopStart) * 1e-6);
        Logger.recordOutput("OpMode/logMs", (logEnd - logStart - userCodeTime) * 1e-6);
    }
}
