package ravenrobotics.shootloops.commands;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import ravenrobotics.shootloops.subsystems.DriveSubsystem;
import ravenrobotics.shootloops.subsystems.FlywheelSubsystem;
import ravenrobotics.shootloops.subsystems.IntakeSubsystem;

public class RunFlywheelCommand extends Command 
{
    private final IntakeSubsystem intakeSubsystem;
    private final FlywheelSubsystem flywheelSubsystem;
    private final DriveSubsystem driveSubsystem;
    
    public RunFlywheelCommand()
    {
        this.intakeSubsystem = IntakeSubsystem.getInstance();
        this.flywheelSubsystem = FlywheelSubsystem.getInstance();
        this.driveSubsystem = DriveSubsystem.getInstance();

        addRequirements(intakeSubsystem, flywheelSubsystem, driveSubsystem);
    }

    @Override
    public void initialize()
    {
        driveSubsystem.motorsToBrake();
    }

    @Override
    public void execute()
    {
        driveSubsystem.drive(new ChassisSpeeds(0, 0, 0));
        flywheelSubsystem.shootOn();
        while (flywheelSubsystem.getBottomVelocity() < 8500 && flywheelSubsystem.getTopVelocity() < 8000)
        {
            //System.out.println("Velocity:" + flywheelSubsystem.getVelocity());
            continue;
        }
        Timer.delay(0.1);
        intakeSubsystem.runRollersFlywheel();
    }

    @Override
    public void end(boolean interrupted)
    {
        flywheelSubsystem.stopFly();
        intakeSubsystem.stopRollers();
        driveSubsystem.motorsToCoast();
    }
}
