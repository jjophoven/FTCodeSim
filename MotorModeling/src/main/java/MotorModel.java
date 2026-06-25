import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacob
 */
public class MotorModel {
    @FunctionalInterface
    public interface Term {
        double calculate(double velocity, double duty, double batteryVoltage);
    }

    public Term[] terms;

    public MotorModel(Term... terms) {
        this.terms = terms;
    }

    public double predict(double[] coefficients, double velocity, double duty, double batteryVoltage) {
        double output = 0;
        for (int i = 0; i < terms.length; i++) {
            output += coefficients[i] * terms[i].calculate(velocity, duty, batteryVoltage);
        }
        return output;
    }

    double[] fit(double[] accels, double[] vels, double[] duties, double[] batteryVoltages) {
        int n = accels.length;
        Matrix A = new Matrix(n, terms.length);
        for (int i = 0; i < n; i++) {
            double vi = vels[i];
            for (int j = 0; j < terms.length; j++) {
                A.set(i, j, terms[j].calculate(vi, duties[i], batteryVoltages[i]));
            }
        }

        Vector b = new Vector(accels);
        QRDecomposition qr = new QRDecomposition(A);
        Vector theta = qr.solve(b);

        return theta.elements();
    }

    public static MotorModel fromString(String modelString) {
        String[] parts = modelString.split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid motor model format. Expected 'output=expression'");
        }

        java.util.List<Term> terms = new java.util.ArrayList<>();

        char coefficient = 'A';
        while (true) {
            int startIndex = modelString.indexOf(coefficient);
            int endIndex = modelString.indexOf(++coefficient);

            if (endIndex == -1) {
                endIndex = modelString.length() + 1;
            }

            String termExpression = modelString.substring(startIndex-1, endIndex-1).trim();
            terms.add(createTermFromExpression(termExpression));

            if (endIndex == modelString.length() + 1) {
                break;
            }
        }

        return new MotorModel(terms.toArray(new Term[0]));
    }

    private static Term createTermFromExpression(String expr) {
        expr = expr.trim();

        final boolean negate;
        if (expr.startsWith("-")) {
            negate = true;
        } else if (expr.startsWith("+")) {
            negate = false;
        } else {
            negate = false;
        }

        List<Term> multipliedTerms = new ArrayList<>();

        if (expr.contains("sgn(")) {
            int start = expr.indexOf("sgn(");
            int end = expr.indexOf(")", start);

            String functionExpr = expr.substring(start, end + 1);
            String innerExpr = expr.substring(start + 4, end);

            Term innerTerm = createTermFromExpression(innerExpr);

            expr = expr.replace(functionExpr, "");

            multipliedTerms.add(
                    (v, d, b) -> Math.signum(innerTerm.calculate(v, d, b)));
        }

        if (expr.contains("abs(")) {
            int start = expr.indexOf("abs(");
            int end = expr.indexOf(")", start);

            String functionExpr = expr.substring(start, end + 1);
            String innerExpr = expr.substring(start + 4, end);

            Term innerTerm = createTermFromExpression(innerExpr);

            expr = expr.replace(functionExpr, "");

            multipliedTerms.add(
                    (v, d, b) -> Math.abs(innerTerm.calculate(v, d, b)));
        }

        if (expr.contains("v")) {
            multipliedTerms.add((v, d, b) -> v);
        }
        if (expr.contains("u")) {
            multipliedTerms.add((v, d, b) -> d*b);
        }
        if (expr.contains("b")) {
            multipliedTerms.add((v, d, b) -> b);
        }
        if (expr.contains("d")) {
            multipliedTerms.add((v, d, b) -> d);
        }

        return (v,d,b) -> {
            double result = 1;
            for (Term term : multipliedTerms) {
                result *= term.calculate(v, d, b);
            }
            return negate ? -result : result;
        };
    }
}
