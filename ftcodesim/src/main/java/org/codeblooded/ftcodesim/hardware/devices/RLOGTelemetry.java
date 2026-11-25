package org.codeblooded.ftcodesim.hardware.devices;

import androidx.annotation.NonNull;
import dev.nextftc.telemetry.TelemetryBackend;
import dev.nextftc.telemetry.TelemetryEntry;
import dev.nextftc.telemetry.TelemetryType;
import dev.nextftc.util.struct.StructSchema;
import org.psilynx.psikit.core.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RLOGTelemetry implements TelemetryBackend {

    private final Map<String, TelemetryEntry> entries =
            new ConcurrentHashMap<>();

    @NonNull
    @Override
    public TelemetryEntry entry(
            @NonNull String path,
            @NonNull TelemetryType type,
            @NonNull Map<String, ?> properties
    ) {
        return entries.computeIfAbsent(path, key -> new TelemetryEntry() {

            @Override
            public void logBoolean(boolean value, long timestampMicros) {
                Logger.recordOutput(key, value);
            }

            @Override
            public void logInteger(long value, long timestampMicros) {
                Logger.recordOutput(key, value);
            }

            @Override
            public void logFloat(float value, long timestampMicros) {
                Logger.recordOutput(key, value);
            }

            @Override
            public void logDouble(double value, long timestampMicros) {
                Logger.recordOutput(key, value);
            }

            @Override
            public void logString(String value, long timestampMicros) {
                Logger.recordOutput(key, value);
            }

            @Override
            public void logBooleanArray(boolean[] value, long timestampMicros) {
                Logger.recordOutput(key, value.clone());
            }

            @Override
            public void logIntegerArray(long[] value, long timestampMicros) {
                Logger.recordOutput(key, value.clone());
            }

            @Override
            public void logFloatArray(float[] value, long timestampMicros) {
                Logger.recordOutput(key, value.clone());
            }

            @Override
            public void logDoubleArray(double[] value, long timestampMicros) {
                Logger.recordOutput(key, value.clone());
            }

            @Override
            public void logStringArray(List<String> value, long timestampMicros) {
                Logger.recordOutput(key, value.toArray(new String[0]));
            }

            @Override
            public <T> void logStruct(
                    StructSchema<T> schema, T value, long timestampMicros
            ) {
                Logger.recordOutput(key+ "/Struct", String.valueOf(value));
            }

            @Override
            public <T> void logStructArray(
                    StructSchema<T> schema,
                    List<? extends T> values,
                    long timestampMicros
            ) {
                Logger.recordOutput(key + "/Struct", values.toString());
            }
        });
    }

    @Override
    public void close() {
        entries.clear();
    }
}