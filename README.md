# JoyConKt

Utilized `hidapi` for JoyCon and Pro Controller.

## Unique Point
`JoyConKt` does not include `GLFW`. 

`GLFW` is the famous library used for GUI framework. 
It also provides gamepad support. 
Some GUI framework include `GLFW` does not provide gamepad support. 
A library for gamepad support is required in these case.
However, some gamepad libraries like [LWJGL](https://www.lwjgl.org/) include `GLFW`. 
It causes the failure of `glfwInit()`. 

`JoyConKt` does not include `GLFW` so that it can be used along with GUI frameworks.

## Features
- Detect connection and disconnection of controllers: `Manager`
- Receive input status includes IMU at 120 Hz or 60 Hz: `StandardFullMode`
- Rumble: `Controller.rumble()`, `Rumble`
- Set player LEDs: `Controller.setPlayerLights()`, `PlayerLight`

## Todo
- [ ] Receive input status when changes: `NormalMode`
- [ ] JoyCon support
- [ ] Dual JoyCon support
- [ ] Calibration support
   - Sticks
   - IMU

## Reference
- [dekuNukem/Nintendo_Switch_Reverse_Engineering](https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering)
- [KaiseiYokoyama/joycon-rs](https://github.com/KaiseiYokoyama/joycon-rs)