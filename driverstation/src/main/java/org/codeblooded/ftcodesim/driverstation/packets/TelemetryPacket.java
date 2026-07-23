package org.codeblooded.ftcodesim.driverstation.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TelemetryPacket implements Packet {
    // TODO allow clearing telemetry updates

    public final String telemetry;

    public TelemetryPacket(String telemetry) {
        this.telemetry = telemetry;
    }

    @Override
    public byte getPacketType() {
        return Packet.TELEMETRY;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeUTF(telemetry);
    }

    public static TelemetryPacket read(DataInputStream input) throws IOException {
        return new TelemetryPacket(input.readUTF());
    }
}