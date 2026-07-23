package org.codeblooded.ftcodesim.ascope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public enum SourceType {
    GREEN_ARTIFACT("gamePiece", "Translation3d", new ObjectMapper().createObjectNode().put("variant", "Artifact (Green)")),
    PURPLE_ARTIFACT("gamePiece", "Translation3d", new ObjectMapper().createObjectNode().put("variant", "Artifact (Purple)")),
    ROBOT_CODE_BLOODED_DECODE("robot", "Pose2d", new ObjectMapper().createObjectNode().put("model", "CodeBloodedDecode")),
    ROBOT_TANK("robot", "Pose2d", new ObjectMapper().createObjectNode().put("model", "Tank")),
    ROBOT_MECANUM_BASE("robot", "Pose2d", new ObjectMapper().createObjectNode().put("model", "Drive Base"));

    public final String type;
    public final String logType;
    public final ObjectNode options;

    SourceType(String type, String logType, ObjectNode options) {
        this.type = type;
        this.logType = logType;
        this.options = options;
    }
}
