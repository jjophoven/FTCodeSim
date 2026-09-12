# Tank validation

The simulator regression suite and example compilation:

```powershell
./gradlew.bat :ftcodesim:testDebugUnitTest --tests '*TankKinematicsTest' :DrivetrainExamples:compileDebugUnitTestJavaWithJavac
```

Seven regression tests cover wheel ordering, two/four motors, CCW turn signs,
inverse/forward round trips, straight/reverse motion, constant-speed circular
integration, lateral constraints, geometry validation and drivetrain mode switching.

## ApexPathing integration

ApexPathing currently depends on `org.codeblooded:Simulator:SNAPSHOT-9cb1d09`.
It cannot directly substitute this checkout without adapting the test harness.
`apex-tank-harness.patch` records the adaptations used for headless validation
against the local checkout on 2026-09-12. Apply with `git apply --unidiff-zero`; it targets a copy of
`TeamCode/src/test/java`, not production robot code. The source checkout used
includes the tank tests and follower changes present in the shared workspace.

The patch updates moved physics imports, bypasses desktop layout installation,
removes the obsolete mecanum strafe-efficiency setting, maps tank wheels directly
for the CCW convention, and explicitly supplies the four-term motor model expected
by the Apex fixture. Natural deceleration is zero in that fixture because its
motor model already includes Coulomb friction; this avoids counting drag twice.
The original wheel-slot comments describe the published dependency, not this local
adaptation. ApexPathing's installed dependency and source files remain unchanged.

Validation used a Gradle init script to include this repository's `ftcodesim` build,
substitute `org.codeblooded:Simulator` with that project, select the copied test
sources and place build outputs in separate directories. All 13 tests passed:

- `TankMecanumDriveEncodersTest`: 5 tests
- `TankPathFollowingSimulationTest`: 5 tests
- `TankAutoAccuracySimulationTest`: 3 tests

This validates tank motion and follower integration with the explicit fixture motor
model. It does not claim real-world tire-slip fidelity or calibration of the default
motor model. Pose integration is exact for constant twist within each step; velocity
still uses the existing acceleration integrator and collision response.
