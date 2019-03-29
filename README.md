# DRASP

**DRASP is an event-based programming language and integrated development environment (IDE).**

It allows to develop and test programs on a PC and automatically deploy them on a Raspberry PI to run and utilize its hardware features like GPIOs, digital and analog sensors.
Furthermore a primitive electronic-design system for planning wired constructions is integrated but still a major TODO.

It is a private project I work since summer 2017 on.
The IDE interface with its integrated, interactive tutorials were a sample for a course about Human-Machine-Interfaces.

## Sample Screenshots
##### A DRASP demo program running that creates a spline and visualizes it as a graph. This could be used to track some value from an analog sensor for example.
![alt text](https://github.com/Drarra/DRASP/blob/master/github%20images/Dynamic%20spline.PNG)
##### The construction view showing a simple electronic circuit (an IC based mini FM-radio from a Conrad experiment set.
Note the construction view is still in development
![alt text](https://github.com/Drarra/DRASP/blob/master/github%20images/Conrad%20Radio.PNG)
##### The built-in console and a breakpoint message
![alt text](https://github.com/Drarra/DRASP/blob/master/github%20images/Built%20in%20console%20breakpoints%20and%20step%20by%20step.PNG)

## Project State
The project is still a work in progress and I am publishing this a few months early to complete my portfolio for job applications since I finished studying recently.
Yet it could technically be used for developing DRASP programs already. The file format will most likely not change anymore.
Overall the plan is to continue development myself till a fully stable build is reached and then offer the open source community to help adding new functionality.

## Try it out
Easiest way to get started is to download the DEMO directory that includes sample programs and a Runnable JAR. Just start it with

*$ java -jar .\DRASP.jar*

Then read through the "Info and Tutorial" window that should pop up on the first startup. The tutorials that start with "X" are implemented. Note that the first start up may take a while because it extracts some files.

## Highlights of the language system
- Turing complete language base on parametrized elements that consist of underlying java code (see the resources/functionality directory)
- Integrated, self written interpreter for the elements (supports events, variables, loops, breakpoints and step-by-step execution)
- Executable on about any system that supports Java 1.6
- Variables like numbers, strings, lists and interpolated splines
- Natural multithreading by event system; "Barriers" give control over threads

## Highlights of the functionality library
- GPIO control (based on the Pi4J library: https://github.com/Pi4J/pi4j)
- PWM support (Raspi hardware and external IC: The widespread PCA9685 (https://www.adafruit.com/product/815) )
- Supporting common Analog to Digital converters (ADS1X15 and MCP300X)
- Supporting several digital sensors through a single interface for temperature, pressure, acceleration and so on (TODO: require testing): INA219, BMP280, LSM303, MPU6050, BH1750, DS18B20
- Wrapper for some JavaFX features including images, buttons, shapes and interpolated graphs partially with animations and transitions (Note, on a Raspberry Pi that requires to add JFX manually; Automatizing this is a TODO)
- Wrapper for the Telegram Bot API (Discord Bot support is next on TODO list)
- Socket-Based networking (requires more testing and features)

## Highlights of the IDE
- Automated deploy of programs and the runner via SSH (for example onto Raspberry Pi)
- Integrated, interactive tutorials
- User-Editable/Extensible functionality library (the java code is packed and recompiled automatically at startup if changed; requires execution with a JDK)
- Integrated console window and variables-overview at runtime
- Simulation of some hardware features like GPIOs that turn into buttons or lamps for testing on PC
- Tools menu to send commands to linux based systems like the Raspberry Pi
- Constructions view to design electronic systems


## Major points on the TODO list
- Assemble a LICENCE file
- Testing of sensor ICs
- Saving of electronic constructions
- Minor bugfixing in the IDE
- Designing more components
- Redoing wire-pathfinding in construction view 
- Supporting more sensors

Full but unordered list in SharedComponents.java
