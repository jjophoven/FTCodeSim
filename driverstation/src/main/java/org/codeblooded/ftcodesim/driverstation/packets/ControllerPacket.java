package org.codeblooded.ftcodesim.driverstation.packets;

import com.studiohartman.jamepad.ControllerState;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class ControllerPacket implements Packet {
    public final byte[] state;
    public final byte gPadID;

    public float removeDrift(float stick) {
        return Math.abs(stick) < 0.01 ? 0.0f : stick;
    }

    public ControllerPacket(byte gPadID, ControllerState state) {
        ByteBuffer buffer = ByteBuffer.allocate(65);
        this.gPadID = gPadID;
        try {
            int buttons = 0;

            buffer.put((byte) 2);
            buffer.putShort((short) 60);
            buffer.putShort((short) 0);

            buffer.put((byte) 5);
            buffer.putInt(0);
            buffer.putLong(System.currentTimeMillis());

            buffer.putFloat(removeDrift(state.leftStickX));
            buffer.putFloat(removeDrift(-state.leftStickY));
            buffer.putFloat(removeDrift(state.rightStickX));
            buffer.putFloat(removeDrift(-state.rightStickY));
            buffer.putFloat(removeDrift(state.leftTrigger));
            buffer.putFloat(removeDrift(state.rightTrigger));

            buttons = (buttons << 1) + (state.leftStickClick ? 1 : 0);
            buttons = (buttons << 1) + (state.rightStickClick ? 1 : 0);
            buttons = (buttons << 1) + (state.dpadUp ? 1 : 0);
            buttons = (buttons << 1) + (state.dpadDown ? 1 : 0);
            buttons = (buttons << 1) + (state.dpadLeft ? 1 : 0);
            buttons = (buttons << 1) + (state.dpadRight ? 1 : 0);
            buttons = (buttons << 1) + (state.a ? 1 : 0);
            buttons = (buttons << 1) + (state.b ? 1 : 0);
            buttons = (buttons << 1) + (state.x ? 1 : 0);
            buttons = (buttons << 1) + (state.y ? 1 : 0);
            buttons = (buttons << 1) + (state.guide ? 1 : 0);
            buttons = (buttons << 1) + (state.start ? 1 : 0);
            buttons = (buttons << 1) + (state.back ? 1 : 0);
            buttons = (buttons << 1) + (state.lb ? 1 : 0);
            buttons = (buttons << 1) + (state.rb ? 1 : 0);
            buffer.putInt(buttons);
            buffer.put(gPadID);
            buffer.put((byte) 0);
            buffer.put((byte) 0);
            buffer.putFloat(0.0f);
            buffer.putFloat(0.0f);
            buffer.putFloat(0.0f);
            buffer.putFloat(0.0f);
        } catch (BufferOverflowException e) {
            System.out.println("Frick u u stupid thing, just work bruh!!");
        }
        this.state = buffer.array();
    }

    @Override
    public byte getPacketType() {
        return Packet.CONTROLLER;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.write(gPadID);
        for (byte b : state) {
            out.write(b);
        }
    }
}
