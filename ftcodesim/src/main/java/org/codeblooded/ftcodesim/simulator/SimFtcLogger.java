package org.codeblooded.ftcodesim.simulator;

import android.annotation.SuppressLint;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.rlog.RLOGServer;
import org.psilynx.psikit.core.rlog.RLOGWriter;
import org.psilynx.psikit.ftc.DriverStationLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

// TODO create our own Logger / RLOG server
public class SimFtcLogger {
    public void start(OpMode opMode,
                      int rlogPort,
                      String filename,
                      boolean writeFile,
                      String folder) {
        Logger.reset();

        if (rlogPort > 0) {
            Logger.addDataReceiver(new RLOGServer(rlogPort));
        }

        if (writeFile) {
            String effectiveFolderRaw;
            effectiveFolderRaw = folder;

            String effectiveFilename =
                    filename == null || Objects.equals(filename, "")
                            ? defaultLogFilename(opMode)
                            : filename;

            String effectiveFolder = effectiveFolderRaw.replace('\\', '/');
            if (!effectiveFolder.endsWith("/")) {
                effectiveFolder += "/";
            }

            Logger.addDataReceiver(
                    new RLOGWriter(effectiveFolder, effectiveFilename)
            );
        }

        Logger.start();
        recordOpModeMetadata(opMode);
    }


    @SuppressLint("SimpleDateFormat")
    private String defaultLogFilename(OpMode opMode) {
        return opMode.getClass().getSimpleName() +
                "_log_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date()) +
                ".rlog";
    }

    private void recordOpModeMetadata(OpMode opMode) {
        TeleOp teleOp = opMode.getClass().getAnnotation(TeleOp.class);
        if (teleOp != null) {
            Logger.recordMetadata("OpMode Name", teleOp.name());
            Logger.recordMetadata("OpMode type", "TeleOp");
            return;
        }

        Autonomous auto = opMode.getClass().getAnnotation(Autonomous.class);
        if (auto != null) {
            Logger.recordMetadata("OpMode Name", auto.name());
            Logger.recordMetadata("OpMode type", "Autonomous");
        }
    }

    DriverStationLogger driverStationLogger = new DriverStationLogger();

    /**
     * Call once per loop, after Logger.periodicBeforeUser().
     */
    public void logOncePerLoop(OpMode opMode, boolean isStarted, boolean isStopped) {
        Logger.recordOutput("OpModeControls/isStarted", isStarted);
        Logger.recordOutput("OpModeControls/isStopped", isStopped);

        driverStationLogger.log(
                opMode.gamepad1,
                opMode.gamepad2
        );
    }
}
