

https://github.com/user-attachments/assets/8d33261f-094c-4cb3-9bf3-9e1ee5f3c3f3

## This project is a plug-and-play [**FTC OpMode Simulator**](https://github.com/jjophoven/FTC-Opmode-Simulator) that runs *without* a robot.

# Features
- **Line-by-line debugging** with breakpoints
- **Live TeleOp input** from a local keyboard or controller
- **Emulated Driver Station** with OpMode selection, init, start, stop, and telemetry
- **Simulated mecanum drive** using per-wheel motor acceleration models fit from real-world data
- **Fake hardware layer** that runs existing FTC code without modifying your OpModes
- **Motor modeling and trajectory simulation** for testing motor models, controllers, and robot behavior against real data

# How it works
The simulator sends live robot data to [AdvantageScope](https://docs.advantagescope.org/) for visualization and connects to a local Driver Station application over a socket. The Driver Station provides gamepad input, OpMode controls, and telemetry, allowing existing FTC code to run in a fully simulated environment.

# Usage

1. Download [Advantage Scope](https://github.com/Mechanical-Advantage/AdvantageScope/releases/tag/v26.0.2)
2. Open AdvantageScope and connect to Simulator -> RLOG Server

<img width="400" height=auto alt="image" src="https://github.com/user-attachments/assets/fe478143-4ad4-4e2c-a66f-187386090772" />

3. In the top right corner click the `+` icon and select if you want a line graph, 2d field, or 3d field.
<img width="380" height="531" alt="image" src="https://github.com/user-attachments/assets/1af1272b-9c27-46ae-a98e-af6cb262e044" />

Make sure you set it to the right field selected.

<img width="516" height="677" alt="image" src="https://github.com/user-attachments/assets/66377cb6-4f30-4afe-8a9c-8be97795b654" />

4. Run `TeamCode/src/test/java/SimulateMecanum` and press init and start.

4. Then drag fields you want to graph/visualize

<img width="1162" height="942" alt="image" src="https://github.com/user-attachments/assets/53f83d85-935d-4d3e-95d8-ec1b36ff4a02" />

# Looking for help with
- Anyone interested in collaborating or working on a similar project
- Ball physics and collision detection
- Improving the Driver Station UI
- New features or ideas
- Packaging/distribution (Gradle plugin, library, installer, etc.)
- Better project architecture or module organization
