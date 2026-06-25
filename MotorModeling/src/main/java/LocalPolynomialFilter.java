
public class LocalPolynomialFilter {
    private final int radius;
    private final int degree;

    public LocalPolynomialFilter(int windowSize, int degree) {
        if (windowSize % 2 == 0)
            throw new IllegalArgumentException("Window size must be odd.");

        this.radius = windowSize / 2;
        this.degree = degree;
    }

    public Result filter(double[] velocities, double[] times) {
        int n = velocities.length;
        double[] filtered = new double[n];
        double[] acceleration = new double[n];

        for (int center = 0; center < n; center++) {

            int start = Math.max(0, center - radius);
            int end = Math.min(n - 1, center + radius);

            int m = end - start + 1;

            Matrix A = new Matrix(m, degree + 1);
            Vector y = new Vector(m);

            double t0 = times[center];

            for (int i = 0; i < m; i++) {

                double dt = times[start + i] - t0;

                double power = 1.0;
                for (int j = 0; j <= degree; j++) {
                    A.set(i, j, power);
                    power *= dt;
                }

                y.set(i, velocities[start + i]);
            }

            Vector coeffs = new QRDecomposition(A).solve(y);

            filtered[center] = coeffs.get(0);
            if (degree > 0)
                acceleration[center] = coeffs.get(1);
        }

        return new Result(filtered, acceleration);
    }

    public static class Result {
        public final double[] velocity;
        public final double[] acceleration;

        public Result(double[] velocity, double[] acceleration) {
            this.velocity = velocity;
            this.acceleration = acceleration;
        }
    }
}