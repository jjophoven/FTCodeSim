/**
 * Computes the QR Decomposition of an m x n matrix (m >= n).
 * Decomposes A into an orthogonal matrix Q and an upper-triangular matrix R.
 *
 * @author Havish or ai
 */
public class QRDecomposition {
    private final Matrix Q;
    private final Matrix R;

    /**
     * Computes the QR decomposition of the given matrix.
     * @param A The matrix to decompose (rows must be >= columns).
     */
    public QRDecomposition(Matrix A) {
        int m = A.getRows();
        int n = A.getColumns();
        if (m < n) {
            throw new IllegalArgumentException("QR decomposition requires rows >= columns.");
        }

        Q = new Matrix(m, n);
        R = new Matrix(n, n);

        // Copy A's columns into Q to use as working vectors
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                Q.set(i, j, A.get(i, j));
            }
        }

        // Modified Gram-Schmidt Orthogonalization
        for (int i = 0; i < n; i++) {
            // Find the Euclidean norm of column i in Q
            double norm = 0;
            for (int k = 0; k < m; k++) {
                norm += Q.get(k, i) * Q.get(k, i);
            }
            norm = Math.sqrt(norm);
            R.set(i, i, norm);

            // Normalize column i of Q
            for (int k = 0; k < m; k++) {
                if (norm > 1e-9) {
                    Q.set(k, i, Q.get(k, i) / norm);
                } else {
                    Q.set(k, i, 0);
                }
            }

            // Project onto remaining columns
            for (int j = i + 1; j < n; j++) {
                double dot = 0;
                for (int k = 0; k < m; k++) {
                    dot += Q.get(k, i) * Q.get(k, j);
                }
                R.set(i, j, dot);

                // Subtract projection from column j
                for (int k = 0; k < m; k++) {
                    Q.set(k, j, Q.get(k, j) - dot * Q.get(k, i));
                }
            }
        }
    }

    public Matrix getQ() { return Q; }
    public Matrix getR() { return R; }

    /**
     * Solves the system A * x = b (or finds the least-squares solution if overdetermined).
     * Transforms the problem to: R * x = Q^T * b
     * @param b The right-hand side target Vector.
     * @return The solution Vector x.
     */
    public Vector solve(Vector b) {
        int m = Q.getRows();
        int n = Q.getColumns();
        if (b.size() != m) {
            throw new IllegalArgumentException("Vector size must match matrix row count.");
        }

        // Compute y = Q^T * b
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int k = 0; k < m; k++) {
                sum += Q.get(k, i) * b.get(k);
            }
            y[i] = sum;
        }

        // Back Substitution: R * x = y
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = y[i];
            for (int k = i + 1; k < n; k++) {
                sum -= R.get(i, k) * x[k];
            }
            if (Math.abs(R.get(i, i)) < 1e-9) {
                throw new ArithmeticException("Matrix is singular or near-singular.");
            }
            x[i] = sum / R.get(i, i);
        }

        return new Vector(x);
    }
}