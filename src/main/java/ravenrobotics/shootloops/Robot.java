// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package ravenrobotics.shootloops;

import com.revrobotics.REVPhysicsSim;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class Robot extends TimedRobot {
  private Command autoCommand;

  private RobotContainer robotContainer;

  @Override
  public void robotInit() 
  {
    //Setup the data log.
    DataLogManager.start();
    DriverStation.startDataLog(DataLogManager.getLog());

    //Initialize the robot container.
    robotContainer = new RobotContainer();
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void disabledExit() {}

  @Override
  public void autonomousInit() {
    //Get the auto command.
    autoCommand = robotContainer.getAutonomousCommand();

    //Schedule the command if it exists.
    if (autoCommand != null) {
      autoCommand.schedule();
    }
  }

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void autonomousExit() {}

  @Override
  public void teleopInit() {
    //Cancel the auto command if it's scheduled.
    if (autoCommand != null) {
      autoCommand.cancel();
    }
    //Setup the robot for teleop.
    robotContainer.setupTeleop();
  }

  @Override
  public void teleopPeriodic() {}

  @Override
  public void teleopExit() {}

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }

  @Override
  public void testPeriodic() {}

  @Override
  public void testExit() {}

  @Override
  public void simulationInit()
  {}

  @Override
  public void simulationPeriodic()
  {
    //Run the REV physics simulation (broken, for sim).
    REVPhysicsSim.getInstance().run();
  }
}
