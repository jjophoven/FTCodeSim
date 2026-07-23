package org.codeblooded.fit;

import org.codeblooded.math.LocalPolynomialFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ModelFitter {
    private static class DataSet {
        double[] rawVels;
        double[] vels;
        double[] accels;
        double[] loopTimes;
        double[] batteryVoltages;
        double[] duties;
        double[] applied;
        double[] times;
    }

    private static class FitMetrics {
        double r2;
        double[] predictions;
        double[] residuals;

        FitMetrics(double r2, double[] predictions, double[] residuals) {
            this.r2 = r2;
            this.predictions = predictions;
            this.residuals = residuals;
        }
    }

    private static class AggregatedData {
        double[] vels;
        double[] accels;
        double[] duties;
        double[] batteryVoltages;
        double[] times;
        double[] rawVels;
        double[] applied;
        double[] loopTimes;

        AggregatedData(double[] vels, double[] accels, double[] duties, double[] batteryVoltages,
                      double[] times, double[] rawVels, double[] applied, double[] loopTimes) {
            this.vels = vels;
            this.accels = accels;
            this.duties = duties;
            this.batteryVoltages = batteryVoltages;
            this.times = times;
            this.rawVels = rawVels;
            this.applied = applied;
            this.loopTimes = loopTimes;
        }
    }

    private static AggregatedData aggregateDatasets(String[] paths, int windowSize, int polyDegree) throws FileNotFoundException {
        List<DataSet> datasets = new ArrayList<>();
        for (String path : paths) {
            datasets.add(loadAndFilterAndSmooth(path, windowSize, polyDegree));
        }

        int totalSamples = datasets.stream().mapToInt(d -> d.vels.length).sum();

        double[] vels = new double[totalSamples];
        double[] accels = new double[totalSamples];
        double[] duties = new double[totalSamples];
        double[] batteryVoltages = new double[totalSamples];
        double[] times = new double[totalSamples];
        double[] rawVels = new double[totalSamples];
        double[] applied = new double[totalSamples];
        double[] loopTimes = new double[totalSamples];

        int offset = 0;
        double timeOffset = 0.0;
        final double GAP_MS = 20; // 20 seconds

        for (DataSet d : datasets) {
            int len = d.vels.length;

            System.arraycopy(d.vels, 0, vels, offset, len);
            System.arraycopy(d.accels, 0, accels, offset, len);
            System.arraycopy(d.duties, 0, duties, offset, len);
            System.arraycopy(d.batteryVoltages, 0, batteryVoltages, offset, len);
            System.arraycopy(d.rawVels, 0, rawVels, offset, len);
            System.arraycopy(d.applied, 0, applied, offset, len);
            System.arraycopy(d.loopTimes, 0, loopTimes, offset, len);

            // Shift timestamps so each trial starts 20 s after the previous ends
            for (int i = 0; i < len; i++) {
                times[offset + i] = d.times[i] + timeOffset;
            }

            if (len > 0) {
                timeOffset += d.times[len - 1] + GAP_MS;
            } else {
                timeOffset += GAP_MS;
            }

            offset += len;
        }

        return new AggregatedData(vels, accels, duties, batteryVoltages, times, rawVels, applied, loopTimes);
    }

    public static void fitAndReportToCSV(String[] paths, int windowSize, int polyDegree, String outCsv, MotorModel model) throws IOException {
        AggregatedData data = aggregateDatasets(paths, windowSize, polyDegree);

        double[] coefficients = model.fit(data.accels, data.vels, data.duties, data.batteryVoltages);

        printCoefficients(coefficients);

        FitMetrics metrics = calculateMetrics(model, coefficients, data);
        writeCSVReport(outCsv, data, metrics);

        System.out.printf("R^2 = %.6f\n", metrics.r2);
        System.out.println("Wrote fit CSV: " + outCsv);
    }

    private static void printCoefficients(double[] coefficients) {
        for (int i = 0; i < coefficients.length; i++) {
            System.out.printf("Coefficient %d = %.6f\n", i, coefficients[i]);
        }
    }

    private static FitMetrics calculateMetrics(MotorModel model, double[] coefficients, AggregatedData data) {
        double mean = calculateMean(data.accels);
        double ssr = 0.0;
        double sst = 0.0;
        double[] predictions = new double[data.accels.length];
        double[] residuals = new double[data.accels.length];

        for (int i = 0; i < data.accels.length; i++) {
            predictions[i] = model.predict(coefficients, data.vels[i], data.duties[i], data.batteryVoltages[i]);
            residuals[i] = data.accels[i] - predictions[i];
            ssr += residuals[i] * residuals[i];
            double dev = data.accels[i] - mean;
            sst += dev * dev;
        }

        double r2 = 1.0 - (ssr / sst);
        return new FitMetrics(r2, predictions, residuals);
    }

    private static double calculateMean(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    private static void writeCSVReport(String outCsv, AggregatedData data, FitMetrics metrics) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(outCsv))) {
            w.println("Timestamp,accel radians per second per second,predicted accel radians per second per second,velocity radians per second,applied_voltage volts,residual radians per second per second,raw velocity radians per second");

            for (int i = 0; i < data.accels.length; i++) {
                double time = (data.times != null && i < data.times.length) ? data.times[i] : i;
                w.printf("%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f\n",
                        time, data.accels[i], metrics.predictions[i], data.vels[i],
                        data.applied[i], metrics.residuals[i], data.rawVels[i]);
            }
        }
    }

//    private static DataSet loadAndFilterAndSmooth(String path, int windowSize, int polyDegree) throws FileNotFoundException {
//        File file = new File(path);
//        if (!file.exists()) throw new FileNotFoundException("Could not find file: " + file.getAbsolutePath());
//
//        List<double[]> dataRows = parseCSVFile(file);
//
//        DataSet data = new DataSet();
//        data.rawVels = extractColumn(dataRows, 0);
//        data.batteryVoltages = extractColumn(dataRows, 1);
//        data.duties = extractColumn(dataRows, 2);
//        data.loopTimes = extractColumn(dataRows, 3);
//        data.times = extractColumn(dataRows, 4);
//        data.applied = extractColumn(dataRows, 5);
//
//        LocalPolynomialFilter filter = new LocalPolynomialFilter(windowSize, polyDegree);
//        LocalPolynomialFilter.Result result = filter.filter(data.rawVels, data.times);
//
//        data.vels = result.velocity;
//        data.accels = result.acceleration;
//
//        return data;
//    }

//    private static DataSet loadAndFilterAndSmooth(String path, int windowSize, int polyDegree) throws FileNotFoundException {
//        File file = new File(path);
//        if (!file.exists()) throw new FileNotFoundException("Could not find file: " + file.getAbsolutePath());
//
//        List<double[]> dataRows = parseCSVFile(file);
//
//        DataSet data = new DataSet();
//        data.rawVels = extractColumn(dataRows, 0);
//        data.batteryVoltages = extractColumn(dataRows, 1);
//        data.duties = extractColumn(dataRows, 2);
//        data.loopTimes = extractColumn(dataRows, 3);
//        data.times = extractColumn(dataRows, 4);
//        data.applied = extractColumn(dataRows, 5);
//
//        data.vels = new double[data.rawVels.length];
//        data.accels = new double[data.rawVels.length];
//
//        final double voltageTolerance = 1.0;
//
//        int start = 0;
//        while (start < data.rawVels.length) {
//            double referenceVoltage = data.applied[start];
//            int end = start + 1;
//
//            while (end < data.rawVels.length &&
//                    Math.abs(data.applied[end] - referenceVoltage) <= voltageTolerance) {
//                end++;
//            }
//
//            int length = end - start;
//
//            if (length >= windowSize) {
//                double[] vels = Arrays.copyOfRange(data.rawVels, start, end);
//                double[] times = Arrays.copyOfRange(data.times, start, end);
//
//                LocalPolynomialFilter filter = new LocalPolynomialFilter(windowSize, polyDegree);
//                LocalPolynomialFilter.Result result = filter.filter(vels, times);
//
//                System.arraycopy(result.velocity, 0, data.vels, start, length);
//                System.arraycopy(result.acceleration, 0, data.accels, start, length);
//            } else {
//                // Segment too short to smooth
//                System.arraycopy(data.rawVels, start, data.vels, start, length);
//
//                for (int i = start; i < end; i++) {
//                    if (i == start || i == data.rawVels.length - 1) {
//                        data.accels[i] = 0;
//                    } else {
//                        data.accels[i] = (data.rawVels[i + 1] - data.rawVels[i - 1])
//                                / (data.times[i + 1] - data.times[i - 1]);
//                    }
//                }
//            }
//
//            start = end;
//        }
//
//        return data;
//    }

    private static DataSet loadAndFilterAndSmooth(String path, int windowSize, int polyDegree) throws FileNotFoundException {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find file: " + file.getAbsolutePath());
        }

        List<double[]> dataRows = parseCSVFile(file);

        DataSet data = new DataSet();
        data.rawVels = extractColumn(dataRows, 0);
        data.batteryVoltages = extractColumn(dataRows, 1);
        data.duties = extractColumn(dataRows, 2);
        data.loopTimes = extractColumn(dataRows, 3);
        data.times = extractColumn(dataRows, 4);
        data.applied = extractColumn(dataRows, 5);

        data.vels = new double[data.rawVels.length];
        data.accels = new double[data.rawVels.length];

        final double VOLTAGE_THRESHOLD = 1.0;

        int start = 0;
        while (start < data.rawVels.length) {

            double referenceVoltage = data.applied[start];
            int end = start + 1;

            while (end < data.rawVels.length &&
                    Math.abs(data.applied[end] - referenceVoltage) <= VOLTAGE_THRESHOLD) {
                end++;
            }

            int length = end - start;

            double[] segVel = Arrays.copyOfRange(data.rawVels, start, end);
            double[] segTime = Arrays.copyOfRange(data.times, start, end);

            // Window cannot exceed segment length and must be odd.
            int segmentWindow = Math.min(windowSize, length);
            if ((segmentWindow & 1) == 0) {
                segmentWindow--;
            }

            if (segmentWindow >= polyDegree + 2) {
                LocalPolynomialFilter filter =
                        new LocalPolynomialFilter(segmentWindow, polyDegree);

                LocalPolynomialFilter.Result result =
                        filter.filter(segVel, segTime);

                System.arraycopy(result.velocity, 0, data.vels, start, length);
                System.arraycopy(result.acceleration, 0, data.accels, start, length);
            } else {
                // Segment too short to smooth.
                System.arraycopy(segVel, 0, data.vels, start, length);

                Arrays.fill(data.accels, start, end, 0.0);
                for (int i = start + 1; i < end - 1; i++) {
                    double dv = data.rawVels[i + 1] - data.rawVels[i - 1];
                    double dt = data.times[i + 1] - data.times[i - 1];
                    if (dt != 0.0) {
                        data.accels[i] = dv / dt;
                    }
                }
            }

            start = end;
        }

        return data;
    }

//    private static List<double[]> parseCSVFile(File file) throws FileNotFoundException {
//        List<double[]> rows = new ArrayList<>();
//
//        try (Scanner scanner = new Scanner(file)) {
//            List<String> lines = new ArrayList<>();
//            while (scanner.hasNextLine()) lines.add(scanner.nextLine());
//            if (lines.size() < 2) throw new IllegalArgumentException("CSV contains no data rows: " + file.getPath());
//
//            double firstTimestampMs = Double.NaN;
//
//            for (int i = 1; i < lines.size(); i++) {
//                String[] row = lines.get(i).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
//
//                double timestampS = parse(row, 0);
////                double batteryVoltage = parse(row, 10);
////                double dutyCycle = parse(row, 12);
////                double loopTimeMs = parse(row, 13);
////                double velocity = parse(row, 18);
//
//                double batteryVoltage = parse(row, 9);
//                double dutyCycle      = parse(row, 11);
//                double loopTimeMs     = parse(row, 12);
//                double velocity       = parse(row, 17); // velocity radians per second
//
//                if (Double.isNaN(firstTimestampMs) && !Double.isNaN(timestampS))
//                    firstTimestampMs = timestampS;
//
//                velocity = orDefault(velocity, 0.0);
//
//                if (Math.abs(velocity) < 0.5) continue; // FIXME was 0.2
//
//                double battery = orDefault(batteryVoltage, 13.0);
//                double duty = orDefault(dutyCycle, 0.0);
//                double loop = orDefault(loopTimeMs, 10.0);
//                double time = orDefault(timestampS, 0.0) - (Double.isNaN(firstTimestampMs) ? 0.0 : firstTimestampMs);
//
//                rows.add(new double[]{velocity, battery, duty, loop, time, duty * battery});
//            }
//        }
//
//        return rows;
//    }

    private static List<double[]> parseCSVFile(File file) throws FileNotFoundException {
        List<double[]> rows = new ArrayList<>();

        try (Scanner scanner = new Scanner(file)) {
            List<String> lines = new ArrayList<>();
            while (scanner.hasNextLine()) lines.add(scanner.nextLine());

            if (lines.size() < 2)
                throw new IllegalArgumentException("CSV contains no data rows: " + file.getPath());

            // Parse header
            String[] headers = lines.get(0).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i].trim(), i);
            }

            int timestampCol = getColumn(columns, "Timestamp");
            int batteryCol = getColumn(columns, "battery voltage");
            int dutyCol = getColumn(columns, "duty cycle");
            int loopTimeCol = getColumn(columns, "loop time ms");
            int velocityCol = getColumn(columns, "velocity radians per second");

            double firstTimestampMs = Double.NaN;

            for (int i = 1; i < lines.size(); i++) {
                String[] row = lines.get(i).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                double timestampS = parse(row, timestampCol);
                double batteryVoltage = parse(row, batteryCol);
                double dutyCycle = parse(row, dutyCol);
                double loopTimeMs = parse(row, loopTimeCol);
                double velocity = parse(row, velocityCol);

                if (Double.isNaN(timestampS)
                        || Double.isNaN(batteryVoltage)
                        || Double.isNaN(dutyCycle)
                        || Double.isNaN(loopTimeMs)
                        || Double.isNaN(velocity)) {
                    continue;
                }

                if (Double.isNaN(firstTimestampMs))
                    firstTimestampMs = timestampS;

                if (Math.abs(velocity) < .5)
                    continue;

                double time = timestampS - firstTimestampMs;

                rows.add(new double[]{
                        velocity,
                        batteryVoltage,
                        dutyCycle,
                        loopTimeMs,
                        time,
                        dutyCycle * batteryVoltage
                });
            }
        }

        return rows;
    }

    private static int getColumn(Map<String, Integer> columns, String name) {
        Integer index = columns.get(name);
        if (index == null) {
            throw new IllegalArgumentException("Missing CSV column: " + name);
        }
        return index;
    }

    private static double[] extractColumn(List<double[]> rows, int column) {
        double[] result = new double[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            result[i] = rows.get(i)[column];
        }
        return result;
    }

    private static double orDefault(double value, double defaultValue) {
        return Double.isNaN(value) ? defaultValue : value;
    }

    private static double parse(String[] row, int index) {
        if (index >= row.length) return Double.NaN;
        String s = row[index].trim();
        if (s.isEmpty() || s.equals("null")) return Double.NaN;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return Double.NaN; }
    }
}
