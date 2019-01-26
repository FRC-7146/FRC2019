package io.github.d0048.vision;

import edu.wpi.first.wpilibj.command.Command;

// Just utilizing the command so I don't have to reimplement SendableBase
public class CVDataSource extends Command {
    public SrcTypes src = SrcTypes.ON_BOARD;

    public CVDataSource(SrcTypes s) {
        super();
        this.src = s;
    }

    @Override
    public String toString() {
        switch (src) {
        case ON_BOARD:
            return "ON_BOARD";
        case PI:
            return "PI";
        default:
            return "UNKOWN";
        }
    }

    @Override
    protected boolean isFinished() {
        return true;
    }

}