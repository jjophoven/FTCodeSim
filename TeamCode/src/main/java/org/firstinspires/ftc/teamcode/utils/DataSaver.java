package org.firstinspires.ftc.teamcode.utils;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.Alliance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public final class DataSaver {
    private static final String TAG = "DataSaver";
    private static final File FILE = getDefaultFile();

    private static File getDefaultFile() {
        if (isUnitTest()) {
//            return new File("build/test-data/savedData-test.txt");
            return new File("savedData-test.txt");
        }

        return new File(AppUtil.ROBOT_DATA_DIR, "savedData.txt");
    }

    private static boolean isUnitTest() {
        return System.getProperty("surefire.test.class.path") != null
                || System.getProperty("java.class.path", "").contains("junit");
    }

    private DataSaver() {}

    public static void save(String data) {
        try (FileWriter writer = new FileWriter(FILE, false)) {
            writer.write(data);
        } catch (Exception e) {
            RobotLog.ee(TAG, e, "Failed to save data");
            System.out.println("Failed to save data");
        }
    }

    public static void save(Pose pose) {
        save(
                pose.getX() + "," +
                        pose.getY() + "," +
                        pose.getHeading()
        );
    }

    public static void save(Pose pose, Alliance alliance) {
        save(
                pose.getX() + "," +
                        pose.getY() + "," +
                        pose.getHeading() + "," +
                        alliance.name()
        );
    }

    public static SavedData load() {
        if (!FILE.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) return null;

            String[] p = line.split(",");

            Pose pose = p.length >= 3
                    ? new Pose(
                    Double.parseDouble(p[0]),
                    Double.parseDouble(p[1]),
                    Double.parseDouble(p[2]))
                    : null;

            Alliance alliance = p.length >= 4
                    ? Alliance.valueOf(p[3])
                    : null;

            return new SavedData(pose, alliance);
        } catch (Exception e) {
           // RobotLog.ee(TAG, e, "Failed to load data");
            return null;
        }
    }

    public static Pose loadPose() {
        SavedData data = load();
        return data == null ? null : data.pose;
    }

    public static Alliance loadAlliance() {
        SavedData data = load();
        return data == null ? null : data.alliance;
    }

    public static final class SavedData {
        public final Pose pose;
        public final Alliance alliance;

        public SavedData(Pose pose, Alliance alliance) {
            this.pose = pose;
            this.alliance = alliance;
        }
    }
}