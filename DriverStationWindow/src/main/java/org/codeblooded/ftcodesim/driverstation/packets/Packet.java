package org.codeblooded.ftcodesim.driverstation.packets;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Packet {
    byte getPacketType();
    void write(DataOutputStream out) throws IOException;

    byte TELEMETRY = 1;
    byte KEY = 2;
    byte STATE = 3;
    byte INIT_OPMODE = 4;
    byte CONTROLLER = 5;
    byte OPMODE_COMMAND = 6;
}