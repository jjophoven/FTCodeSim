package org.codeblooded.ftcodesim.ascope;


import org.psilynx.psikit.core.Logger;
import org.psilynx.psikit.core.wpi.math.Translation3d;

public class GamePiece {
    SourceType type;
    Translation3d position;

    public GamePiece(SourceType type, Translation3d position) {
        this.type = type;
        this.position = position;
    }

    public static GamePiece greenArtifact(double x, double y) {
        return new GamePiece(SourceType.GREEN_ARTIFACT, new Translation3d(x-2.25, y+0.25, 4.9/2));
    }

    public static GamePiece purpleArtifact(double x, double y) {
        return new GamePiece(SourceType.PURPLE_ARTIFACT, new Translation3d(x-2.25, y+0.25, 4.9/2));
    }

    public void log(String key) {
        Logger.recordOutput("Field/GamePieces/" + type.name() + key + " Pedro coords (inches)", position);
        double inchesPerMeter = 39.37;
        double halfField = 141.5 / 2;
        Translation3d pedroCoords = new Translation3d(-(position.getY() - halfField) / inchesPerMeter, (position.getX() - halfField) / inchesPerMeter, position.getZ() / inchesPerMeter);
        Logger.recordOutput("Field/GamePieces/" + type.name() + key + " ftc coords (m)", pedroCoords);

        AdvantageScopeRunner.INSTANCE.addSource("RealOutputs/Field/GamePieces/" + type.name() + key + " ftc coords (m)", type);
    }
}
