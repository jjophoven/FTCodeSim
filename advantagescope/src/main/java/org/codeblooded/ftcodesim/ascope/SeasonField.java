package org.codeblooded.ftcodesim.ascope;

import org.codeblooded.ftcodesim.ascope.boundaries.MotionVector;

public enum SeasonField {
    DECODE("FTC:2025-2026 Field", new MotionVector[]{
            new MotionVector(0, 0),
            new MotionVector(0, 68.8),
            new MotionVector(7.6, 68.8),
            new MotionVector(7.6, 118.8),
            new MotionVector(23.4, 141.3),
            new MotionVector(117.9, 141.3),
            new MotionVector(133.8, 118.8),
            new MotionVector(133.8, 68.8),
            new MotionVector(141.3, 68.8),
            new MotionVector(141.3, 0)
    }, new GamePiece[]{
            GamePiece.greenArtifact(2.89, 8.17),
            GamePiece.greenArtifact(138.5, 8.17),

            GamePiece.greenArtifact(18, 82.5),
            GamePiece.greenArtifact(23, 58.5),
            GamePiece.greenArtifact(28, 35.5),

            GamePiece.greenArtifact(122.5, 82.5),
            GamePiece.greenArtifact(117.5, 58.5),
            GamePiece.greenArtifact(112.5, 35.5),

            GamePiece.purpleArtifact(2.89, 13.17),
            GamePiece.purpleArtifact(2.89, 3.17),
            GamePiece.purpleArtifact(138.5, 13.17),
            GamePiece.purpleArtifact(138.5, 3.17),

            GamePiece.purpleArtifact(23, 82.5),
            GamePiece.purpleArtifact(28, 82.5),
            GamePiece.purpleArtifact(18, 58.5),

            GamePiece.purpleArtifact(28, 58.5),
            GamePiece.purpleArtifact(18, 35.5),
            GamePiece.purpleArtifact(23, 35.5),

            GamePiece.purpleArtifact(112.5, 82.5),
            GamePiece.purpleArtifact(117.5, 82.5),
            GamePiece.purpleArtifact(112.5, 58.5),

            GamePiece.purpleArtifact(122.5, 58.5),
            GamePiece.purpleArtifact(117.5, 35.5),
            GamePiece.purpleArtifact(122.5, 35.5),
    });

    public final String ascopeName;
    public final MotionVector[] boundary; // TODO add multiple boundaries for things like submersible
    public final GamePiece[] gamePieces;

    SeasonField(String ascopeName, MotionVector[] boundary, GamePiece[] gamePieces) {
        this.ascopeName = ascopeName;
        this.boundary = boundary;
        this.gamePieces = gamePieces;
    }
}
