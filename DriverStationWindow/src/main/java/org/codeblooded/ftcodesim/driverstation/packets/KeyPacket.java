package org.codeblooded.ftcodesim.driverstation.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class KeyPacket implements Packet {
    public final int keyCode;
    public final boolean down;

    public KeyPacket(int keyCode, boolean down) {
        this.keyCode = keyCode;
        this.down = down;
    }

    @Override
    public byte getPacketType() {
        return Packet.KEY;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(keyCode);
        out.writeBoolean(down);
    }

    public static KeyPacket read(DataInputStream in) throws IOException {
        return new KeyPacket(
                in.readInt(),
                in.readBoolean()
        );
    }
}