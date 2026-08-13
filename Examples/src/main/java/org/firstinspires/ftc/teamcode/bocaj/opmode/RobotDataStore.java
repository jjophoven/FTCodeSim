package org.firstinspires.ftc.teamcode.bocaj.opmode;

import androidx.annotation.Nullable;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public final class RobotDataStore {
    private static final String TAG = "RobotDataStore";
    private static final File FILE = getDefaultFile();
    private static Map<String, String> data;

    private RobotDataStore() {}

    private static File getDefaultFile() {
        if (isUnitTest()) {
            return new File("robotDataStore-test.txt");
        }

        return new File(AppUtil.ROBOT_DATA_DIR, "robotDataStore.txt");
    }

    private static boolean isUnitTest() {
        return System.getProperty("surefire.test.class.path") != null
                || System.getProperty("java.class.path", "").contains("junit");
    }

    public static void put(Map<String, String> data) {
        try (FileWriter writer = new FileWriter(FILE, false)) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
        } catch (Exception e) {
            RobotLog.ee(TAG, e, "Failed to save data");
        }
    }

    public static void put(String key, Object value) {
        if (data == null) data = new HashMap<>();

        data.put(key, String.valueOf(value));
        put(data);
    }

    public static void loadData() {
        data = new HashMap<>();

        if (!FILE.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line;

            while ((line = reader.readLine()) != null) {
                int split = line.indexOf('=');

                if (split <= 0) continue;

                String key = line.substring(0, split);
                String value = line.substring(split + 1);

                data.put(key, value);
            }
        } catch (Exception e) {
            RobotLog.ee(TAG, e, "Failed to load data");
        }
    }

    public static String get(String key) {
        return data.get(key);
    }

    public static @Nullable Double getDouble(String key) {
        try {
            return Double.parseDouble(get(key));
        } catch (NullPointerException | NumberFormatException e) {
            return null;
        }
    }

    public static @Nullable Integer getInt(String key) {
        try {
            return Integer.parseInt(get(key));
        } catch (NullPointerException | NumberFormatException e) {
            return null;
        }
    }

    public static @Nullable Boolean getBoolean(String key) {
        try {
            return Boolean.parseBoolean(get(key));
        } catch (NullPointerException | NumberFormatException e) {
            return null;
        }
    }
}