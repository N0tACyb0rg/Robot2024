// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package ravenrobotics.shootloops;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.StartEndCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import ravenrobotics.shootloops.Constants.DriverStationConstants;
import ravenrobotics.shootloops.Constants.KinematicsConstants;
import ravenrobotics.shootloops.commands.DriveCommand;
import ravenrobotics.shootloops.commands.RunFlywheelCommand;
import ravenrobotics.shootloops.commands.autos.DriveForCommand;
import ravenrobotics.shootloops.commands.autos.DriveForwardAuto;
import ravenrobotics.shootloops.commands.autos.ppcommands.*;
import ravenrobotics.shootloops.subsystems.ClimberSubsystem;
import ravenrobotics.shootloops.subsystems.DriveSubsystem;
import ravenrobotics.shootloops.subsystems.IMUSubsystem;
import ravenrobotics.shootloops.subsystems.IntakeSubsystem;
import ravenrobotics.shootloops.subsystems.RGBSubsystem;
import ravenrobotics.shootloops.subsystems.ClimberSubsystem.ClimberDirection;
import ravenrobotics.shootloops.subsystems.IntakeSubsystem.IntakeArmPosition;
import ravenrobotics.shootloops.subsystems.RGBSubsystem.RGBValues;
import ravenrobotics.shootloops.util.Telemetry;

public class RobotContainer 
{
  //Driver controller (drives the robot around).
  private final CommandXboxController driverController = new CommandXboxController(DriverStationConstants.kDriverPort);
  private final CommandXboxController systemsController = new CommandXboxController(DriverStationConstants.kSystemsPort);

  //Whether to drive field relative or not.
  public boolean isFieldRelative = false;
  private GenericEntry isFieldRelativeEntry = Telemetry.teleopTab.add("Field Relative", false).getEntry();

  private final RunFlywheelCommand flywheelCommand = new RunFlywheelCommand();

  private final SequentialCommandGroup driveOutAutoRight = new SequentialCommandGroup(
    new AutoShootCommand(),
    new DriveForCommand(2, 0.05, 5)
  );

  private final SequentialCommandGroup driveOutAutoLeft = new SequentialCommandGroup(
    new AutoShootCommand(),
    new DriveForCommand(2, -0.05, 5)
  );

  private final SendableChooser<Command> autoChooser = new SendableChooser<Command>();

  //Main drive command.
  private final DriveCommand driveCommand = new DriveCommand(
    () -> -driverController.getLeftY(),
    () -> -driverController.getLeftX(),
    () -> -driverController.getRightX(),
    () -> isFieldRelative);

  public RobotContainer()
  {
    DriveSubsystem.getInstance().configPathPlanner();

    NamedCommands.registerCommand("lineUpSpeaker", new LineUpWithSpeakerCommand());
    NamedCommands.registerCommand("shootNote", new AutoShootCommand());
    NamedCommands.registerCommand("intakeNote", new IntakeNoteCommand());

    RGBSubsystem.getInstance().setPattern(RGBValues.kDefault);

    //autoChooser = AutoBuilder.buildAutoChooser();

    autoChooser.addOption("Drive Forward", new DriveForwardAuto());
    autoChooser.addOption("Shoot then Drive Out Right", driveOutAutoRight);
    autoChooser.addOption("Shoot then Drive Out Left", driveOutAutoLeft);

    Telemetry.teleopTab.add("Auto Chooser", autoChooser);

    //Configure configured controller bindings.
    configureBindings();
    DriveSubsystem.getInstance().setDefaultCommand(driveCommand);
  }

  private void configureBindings()
  {
    //Set the buttons on the joystick for field-relative and zeroing the heading.
    driverController.back().onTrue(new InstantCommand(() -> toggleFieldRelative()));
    driverController.start().onTrue(new InstantCommand(() -> IMUSubsystem.getInstance().zeroYaw()));

    driverController.leftTrigger().whileTrue(new StartEndCommand(() -> ClimberSubsystem.getInstance().moveLeft(ClimberDirection.kUp), () -> ClimberSubsystem.getInstance().stopMotors()));
    driverController.leftBumper().whileTrue(new StartEndCommand(() -> ClimberSubsystem.getInstance().moveLeft(ClimberDirection.kDown), () -> ClimberSubsystem.getInstance().stopMotors()));

    driverController.rightTrigger().whileTrue(new StartEndCommand(() -> ClimberSubsystem.getInstance().moveRight(ClimberDirection.kUp), () -> ClimberSubsystem.getInstance().stopMotors()));
    driverController.rightBumper().whileTrue(new StartEndCommand(() -> ClimberSubsystem.getInstance().moveRight(ClimberDirection.kDown), () -> ClimberSubsystem.getInstance().stopMotors()));

    driverController.y().onTrue(new InstantCommand(() -> DriveSubsystem.getInstance().setCenterOfRotation(KinematicsConstants.kFrontLeftOffset)));
    driverController.b().onTrue(new InstantCommand(() -> DriveSubsystem.getInstance().setCenterOfRotation(KinematicsConstants.kFrontRightOffset)));
    driverController.x().onTrue(new InstantCommand(() -> DriveSubsystem.getInstance().setCenterOfRotation(KinematicsConstants.kBackLeftOffset)));
    driverController.a().onTrue(new InstantCommand(() -> DriveSubsystem.getInstance().setCenterOfRotation(KinematicsConstants.kBackRightOffset)));

    driverController.rightStick().onTrue(new InstantCommand(() -> DriveSubsystem.getInstance().resetCenterofRotation()));


    systemsController.x().onTrue(IntakeSubsystem.getInstance().intakeRoutineCommand);
    systemsController.b().onTrue(new InstantCommand(() -> IntakeSubsystem.getInstance().intakeRoutineCommand.cancel()));

    systemsController.leftTrigger().onTrue(new InstantCommand(() -> IntakeSubsystem.getInstance().runRollersIntake()));
    systemsController.leftTrigger().onFalse(new InstantCommand(() -> IntakeSubsystem.getInstance().stopRollers()));

    systemsController.rightTrigger().onTrue(flywheelCommand);
    systemsController.rightTrigger().onFalse(new InstantCommand(() -> flywheelCommand.cancel()));
  }

  public void setupTeleop()
  {
    DriveSubsystem.getInstance().motorsToCoast();
    // ClimberSubsystem.getInstance().bothDown();
  }

  private void toggleFieldRelative()
  {
    //Toggle field relative (if true set false, if false set true)
    if (isFieldRelative)
    {
      isFieldRelative = false;
      isFieldRelativeEntry.setBoolean(isFieldRelative);
    }
    else
    {
      isFieldRelative = true;
      isFieldRelativeEntry.setBoolean(isFieldRelative);
    }
  }

  public Command getAutonomousCommand()
  {
    DriveSubsystem.getInstance().motorsToBrake();
    //ClimberSubsystem.getInstance().bothDown();
    return autoChooser.getSelected();
  }
}
