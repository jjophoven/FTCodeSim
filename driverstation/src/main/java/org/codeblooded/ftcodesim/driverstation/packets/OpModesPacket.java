package org.codeblooded.ftcodesim.driverstation.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpModesPacket implements Packet {
    public List<InitOpModePacket> opmodes;

    public OpModesPacket(List<InitOpModePacket> opmodes) {
        this.opmodes = opmodes;
    }

    @Override
    public byte getPacketType() {
        return Packet.INIT_OPMODE;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        try {
            output.writeInt(opmodes.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (InitOpModePacket opmode : opmodes) {
            opmode.write(output);
        }
    }

    public static OpModesPacket read(DataInputStream input) {
        List<InitOpModePacket> opmodes = new ArrayList<>();

        int autoCount;
        try {
            autoCount = input.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < autoCount; i++) {
            opmodes.add(InitOpModePacket.read(input));
        }

        return new OpModesPacket(opmodes);
    }
}
