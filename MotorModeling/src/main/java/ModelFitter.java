import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    public static void main(String[] args) throws IOException {
        String[] paths = (args.length > 0) ? args : new String[] {"MotorModeling/+1to.0001.csv"};

        int windowSize = 41;
        int polyDegree = 2;

        List<DataSet> datasets = new ArrayList<>();
        for (String path : paths) {
            datasets.add(loadAndFilterAndSmooth(path, windowSize, polyDegree));
            // TODO add time between trails
        }

        AggregatedData aggregated = aggregateDatasets(datasets);
        fitAndReportToCSV(aggregated, "fit_forward.csv");
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

    private static AggregatedData aggregateDatasets(List<DataSet> datasets) {
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
        for (DataSet d : datasets) {
            int len = d.vels.length;
            System.arraycopy(d.vels, 0, vels, offset, len);
            System.arraycopy(d.accels, 0, accels, offset, len);
            System.arraycopy(d.duties, 0, duties, offset, len);
            System.arraycopy(d.batteryVoltages, 0, batteryVoltages, offset, len);
            System.arraycopy(d.times, 0, times, offset, len);
            System.arraycopy(d.rawVels, 0, rawVels, offset, len);
            System.arraycopy(d.applied, 0, applied, offset, len);
            System.arraycopy(d.loopTimes, 0, loopTimes, offset, len);
            offset += len;
        }

        return new AggregatedData(vels, accels, duties, batteryVoltages, times, rawVels, applied, loopTimes);
    }

    private static void fitAndReportToCSV(AggregatedData data, String outCsv) throws IOException {
        // v -> velocity, d -> duty cycle, b -> battery voltage
//        MotorModel model = new MotorModel(
//                (v, d, b) -> d * b,                    // motor torque
//                (v, d, b) -> -v * Math.abs(d),        // back emf (only applied for on PWM)
//                (v, d, b) -> -v,                       // viscous friction
//                (v, d, b) -> -Math.signum(v)          // coulomb friction
//        );

        MotorModel model = MotorModel.fromString("a=Au-Bv*abs(d)-Cv-Dsgn(v)");

        double[] coefficients = model.fit(data.accels, data.vels, data.duties, data.batteryVoltages);

        printCoefficients(coefficients);

        FitMetrics metrics = calculateMetrics(model, coefficients, data);
        writeCSVReport(outCsv, data, model, coefficients, metrics);

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

    private static void writeCSVReport(String outCsv, AggregatedData data, MotorModel model,
                                       double[] coefficients, FitMetrics metrics) throws IOException {
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

    private static DataSet loadAndFilterAndSmooth(String path, int windowSize, int polyDegree) throws FileNotFoundException {
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("Could not find file: " + file.getAbsolutePath());

        List<double[]> dataRows = parseCSVFile(file);

        DataSet data = new DataSet();
        data.rawVels = extractColumn(dataRows, 0);
        data.batteryVoltages = extractColumn(dataRows, 1);
        data.duties = extractColumn(dataRows, 2);
        data.loopTimes = extractColumn(dataRows, 3);
        data.times = extractColumn(dataRows, 4);
        data.applied = extractColumn(dataRows, 5);

        LocalPolynomialFilter filter = new LocalPolynomialFilter(windowSize, polyDegree);
        LocalPolynomialFilter.Result result = filter.filter(data.rawVels, data.times);

        data.vels = result.velocity;
        data.accels = result.acceleration;

        return data;
    }

    private static List<double[]> parseCSVFile(File file) throws FileNotFoundException {
        List<double[]> rows = new ArrayList<>();

        try (Scanner scanner = new Scanner(file)) {
            List<String> lines = new ArrayList<>();
            while (scanner.hasNextLine()) lines.add(scanner.nextLine());
            if (lines.size() < 2) throw new IllegalArgumentException("CSV contains no data rows: " + file.getPath());

            double firstTimestampMs = Double.NaN;

            for (int i = 1; i < lines.size(); i++) {
                String[] row = lines.get(i).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                double timestampMs = parse(row, 0);
                double batteryVoltage = parse(row, 10);
                double dutyCycle = parse(row, 12);
                double loopTimeMs = parse(row, 13);
                double velocity = parse(row, 18);

                if (Double.isNaN(firstTimestampMs) && !Double.isNaN(timestampMs))
                    firstTimestampMs = timestampMs;

                velocity = orDefault(velocity, 0.0);

                if (Math.abs(velocity) < 0.2) continue;

                double battery = orDefault(batteryVoltage, 13.0);
                double duty = orDefault(dutyCycle, 0.0);
                double loop = orDefault(loopTimeMs, 10.0);
                double time = orDefault(timestampMs, 0.0) - (Double.isNaN(firstTimestampMs) ? 0.0 : firstTimestampMs);

                rows.add(new double[]{velocity, battery, duty, loop, time, duty * battery});
            }
        }

        return rows;
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
