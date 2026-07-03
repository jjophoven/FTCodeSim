import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.rlog.RLOGServer;

@Deprecated
public class MotorSimulations {
    private static final RLOGServer server = new RLOGServer();
    private PhysicsSim sim;

    @Before
    public void setUp() throws InterruptedException {
        sim = new PhysicsSim();

        Logger.addDataReceiver(server);
        Logger.start();
        Logger.setTimeSource(sim::getVirtualTime);

        Thread.sleep(1000);
    }

    @After
    public void tearDown() {
        Logger.end();
    }

    @Test
    public void bangBang() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> sim.bangBang(currentVel, targetPos - currentPos));
    }

    @Test
    public void minimumPowerBangBang() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> sim.minimumBrakePowerBangBang(currentVel, targetPos - currentPos));
    }

    @Test
    public void proportional() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos;
            return error * 0.5;
        });
    }

    @Test
    public void PD() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos;
            return error * 0.5 - currentVel * 0.09;
        });
    }

    @Test
    public void predictiveBrake20Percent() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos - sim.stoppingDistance(currentVel,-0.2) - 0.2;

            return error * 0.5;
        });
    }

    @Test
    public void predictiveBrakeMax() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos - sim.stoppingDistance(currentVel, -1) - (1-sim.minimumPowerToMaxDecel(currentVel, targetPos - currentPos));

            return error * 0.5;
        });
    }

    @Test
    public void predictiveBrakeZero() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos - sim.stoppingDistance(currentVel,0);

            return error * 0.5;
        });
    }

    @Test
    public void predictiveQuadraticBrakeZero() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos - sim.stoppingDistanceQuadratic(currentVel,0);

            return error * 0.5;
        });
    }

    @Test
    public void brakePIDF() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos;
            double targetVel = sim.maxVelocityToStopWithinDistance(error, -0.2);

            Logger.recordOutput("sim/error inches", error);
            Logger.recordOutput("sim/targetVel inches per second", targetVel);

            return (targetVel - currentVel) * 0.025 + (targetVel) * 0.0125 - 0.2 * Math.signum(currentVel);
        });
    }

    @Test
    public void predictivePIDF() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos;
            double targetVel = sim.maxVelocityToStopWithinDistance(error, -0.2);
            double excessVelocityAfterBraking = sim.excessVelocityAfterBraking(error, currentVel, -0.2);

            Logger.recordOutput("sim/error inches", error);
            Logger.recordOutput("sim/targetVel inches per second", targetVel);
            Logger.recordOutput("sim/excessVelocityAfterBraking inches per second", excessVelocityAfterBraking);

            return (targetVel - excessVelocityAfterBraking) * 0.0125 + (targetVel - currentVel) * 0.025 - 0.2 * Math.signum(currentVel);
        });
    }

    @Test
    public void predictivePIDF2() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos;
            double targetVel = sim.maxVelocityToStopWithinDistance(error, -0.2);
            double excessVelocityAfterBraking = sim.excessVelocityAfterBraking(error, currentVel, -0.2);

            Logger.recordOutput("sim/error inches", error);
            Logger.recordOutput("sim/targetVel inches per second", targetVel);
            Logger.recordOutput("sim/excessVelocityAfterBraking inches per second", excessVelocityAfterBraking);

            return (targetVel - excessVelocityAfterBraking) * 0.0125 + (targetVel - currentVel - excessVelocityAfterBraking) * 0.025 - 0.2 * Math.signum(currentVel);
        });
    }


    @Test
    public void PIDFCoastPrediction() throws InterruptedException {
        runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos;
            double targetVel = sim.maxVelocityToStopWithinDistance(error, -0.2);

            Logger.recordOutput("sim/error inches", error);
            Logger.recordOutput("sim/targetVel inches per second", targetVel);

            double discrim = currentVel * currentVel - 2 * Math.abs(error) * sim.zeroPowerAcceleration;
            double finalCoastVel = Math.signum(error) * Math.sqrt(Math.max(0, discrim));
            Logger.recordOutput("sim/finalCoastVel inches per second", finalCoastVel);

            if (Math.signum(finalCoastVel) != Math.signum(currentVel)) {
                finalCoastVel = 0;
            }

            return (targetVel - currentVel) * 0.025 + (targetVel - finalCoastVel) * 0.0125 - 0.2 * Math.signum(currentVel);
        });
    }

    @Test
    public void predictivePIDF2Constrained() throws InterruptedException {
        runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos;
            double targetVel = Math.min(40, sim.maxVelocityToStopWithinDistance(error, -0.2));

            Logger.recordOutput("sim/error inches", error);
            Logger.recordOutput("sim/targetVel inches per second", targetVel);

            double excessBrake = sim.excessVelocityAfterBraking(error, currentVel, -0.2);
            Logger.recordOutput("sim/excessBrake inches per second", excessBrake);
            double output = (targetVel - currentVel - excessBrake) * 0.025 + (targetVel - excessBrake) * 0.0125;
            if (excessBrake > 0) {
                output -= 0.2 * Math.signum(currentVel);
            }

            return output;
        });
    }

    @Test
    public void SquID() throws InterruptedException {
        this.runSimulationTwice((currentPos, currentVel, targetPos) -> {
            double error = targetPos - currentPos;
            return Math.signum(error) * Math.sqrt(Math.abs(error)) * 0.0625;
        });
    }

    /**
     * Functional interface for a simple controller
     */
    interface Controller {
        double calculatePower(double currentPos, double currentVel, double targetPos);
    }

    private void runSimulation(double targetPos, Controller controller) throws InterruptedException {
        do {
            Logger.periodicBeforeUser();

            double power = controller.calculatePower(sim.getPosition(), sim.getSpeed(), targetPos);
            sim.setMotorPower(power);

            sim.update();

            double error = targetPos - sim.getPosition();
            double targetVel = sim.maxVelocityToStopWithinDistance(error, -0.2);

            Logger.recordOutput("sim/error inches", error);
            Logger.recordOutput("sim/targetVel inches per second", targetVel);
            Logger.recordOutput("test/targetPos inches", targetPos);

            Logger.periodicAfterUser(0, 0);
        } while (!(Math.abs(targetPos - sim.getPosition()) < 0.5) || !(Math.abs(sim.getSpeed()) < 0.1));
    }

    private void runSimulationOnce(Controller controller) throws InterruptedException {
        runSimulation(64, controller);
    }

    private void runSimulationTwice(Controller controller) throws InterruptedException {
        runSimulation(12, controller);
        runSimulation(64, controller);
    }
}
