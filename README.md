https://github.com/user-attachments/assets/8d33261f-094c-4cb3-9bf3-9e1ee5f3c3f3

## FTCodesim runs your existing FTC code *without* a physical robot.

# Features
- **Line-by-line debugging** with breakpoints
- **Simulated mecanum drive** using per-wheel motor acceleration models fit from real-world data
- **Live TeleOp input** from a local keyboard or controller
- **Emulated Driver Station** with OpMode selection, init, start, stop, and telemetry
- **Fake hardware layer** that runs existing FTC code without modifying your OpModes
- **Motor modeling and trajectory simulation** for testing motor models, controllers, and robot behavior against real data

# How it works
The simulator sends live robot data to [AdvantageScope](https://docs.advantagescope.org/) for visualization and connects to a local Driver Station application over a socket. The Driver Station provides gamepad input, OpMode controls, and telemetry, allowing existing FTC code to run in a fully simulated environment.

# Usage

1. To the `repositories` block in `build.dependencies.gradle` add:
```gradle
repositories {
    mavenCentral()
    google()
    
    maven {
        url = uri("https://www.jitpack.io")
    }
    maven {
        url = uri("https://repo.dairy.foundation/releases")
    }
    maven {
        url = uri("https://repo.dairy.foundation/snapshots")
    }
}
```
And to the `dependencies` block add:
```gradle
dependencies {
    implementation "org.codeblooded.ftcodesim:Simulator:SNAPSHOT-9cb1d09"
    testImplementation "junit:junit:4.13.2"
}
```

2. Check out the Examples module and run a simulation on your code like [SimulateCodeBloodedDecode](https://github.com/jjophoven/FTCodeSim/blob/master/Examples/src/test/java/SimulateCodeBloodedDecode.java)
3. Once your code is running and you see the Driver Station Window, open  [Advantage Scope](https://github.com/Mechanical-Advantage/AdvantageScope/releases/tag/v26.0.2) and `Connect to Simulator` -> `RLOG Server`

<img width="400" height=auto alt="image" src="https://github.com/user-attachments/assets/fe478143-4ad4-4e2c-a66f-187386090772" />

# Looking for help with
- Anyone interested in collaborating or working on a similar project
- Ball physics and collision detection
- Improving the Driver Station UI
- New features or ideas
- Cleaning up packaging/distribution
- Driver practice support with intaking, shooting, and scoring

![img.png](keymap.png)
