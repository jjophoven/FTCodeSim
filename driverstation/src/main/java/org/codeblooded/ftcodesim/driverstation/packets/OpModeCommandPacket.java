package org.codeblooded.ftcodesim.driverstation.packets;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;


public class OpModeCommandPacket implements Packet {
    public boolean state; // 0 is start, 1 is stop

    public static final OpModeCommandPacket STOP = new OpModeCommandPacket(true);
    public static final OpModeCommandPacket START = new OpModeCommandPacket(false);

    public OpModeCommandPacket(boolean state) {
        this.state = state;
    }

    @Override
    public byte getPacketType() {
        return Packet.OPMODE_COMMAND;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeBoolean(state);
    }

    public static OpModeCommandPacket read(DataInput input) throws IOException {
        return new OpModeCommandPacket(input.readBoolean());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpModeCommandPacket) {
            OpModeCommandPacket other = (OpModeCommandPacket) obj;
            return other.state == state;
        }
        return false;
    }

    @Override
    public String toString() {
        return state ? "Stop" : "Start";
    }
}