package org.codeblooded.ftcodesim.simulator;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.codeblooded.ftcodesim.driverstation.packets.InitOpModePacket;
import org.codeblooded.ftcodesim.driverstation.packets.OpModesPacket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class OpModeRegister {
    Set<OpMode> opmodes = new LinkedHashSet<>();

    public OpModeRegister() {
        findAnnotatedOpModes();
    }

    public InitOpModePacket toPacket(OpMode opMode) {
        if (opMode.getClass().isAnnotationPresent(TeleOp.class)) {
            TeleOp annotation = opMode.getClass().getAnnotation(TeleOp.class);
            assert annotation != null;
            String name = annotation.name();
            if (name.isEmpty()) name = opMode.getClass().getSimpleName();
            return new InitOpModePacket(InitOpModePacket.Type.TELEOP, name, annotation.group());
        }
        if (opMode.getClass().isAnnotationPresent(Autonomous.class)) {
            Autonomous annotation = opMode.getClass().getAnnotation(Autonomous.class);
            assert annotation != null;
            String name = annotation.name();
            if (name.isEmpty()) name = opMode.getClass().getSimpleName();
            return new InitOpModePacket(InitOpModePacket.Type.AUTO, name, annotation.group());
        }
        throw new RuntimeException("opmode is not Teleop or Autonomous annotated");
    }

    public Set<OpMode> getOpModes() {
        return opmodes;
    }

    public OpMode getOpMode(InitOpModePacket packet) {
        for (OpMode opMode : getOpModes()) {
            if (toPacket(opMode).equals(packet)) {
                return opMode;
            }
        }
        throw new RuntimeException("could not find opmode");
    }

    public void writeOpmodes(DataOutputStream output) throws IOException {
        List<InitOpModePacket> opModes = new ArrayList<>();

        for (OpMode opMode : getOpModes()) {
            opModes.add(toPacket(opMode));
        }

        OpModesPacket opModesPacket = new OpModesPacket(opModes);
        output.writeByte(opModesPacket.getPacketType());
        opModesPacket.write(output);
        output.flush();
    }

    private void findAnnotatedOpModes() {
        String[] packagesToScan = new String[] {
                "org.firstinspires.ftc.teamcode"
        };

        String classPath = System.getProperty("java.class.path");
        if (classPath == null) return;
        String[] entries = classPath.split(File.pathSeparator);

        for (String entry : entries) {
            File file = new File(entry);
            if (!file.exists()) continue;
            if (file.isDirectory()) {
                for (String pkg : packagesToScan) {
                    String path = pkg.replace('.', File.separatorChar);
                    File pkgDir = new File(file, path);
                    if (pkgDir.exists() && pkgDir.isDirectory()) {
                        findClassesInDirectory(pkg, pkgDir);
                    }
                }
            } else if (entry.endsWith(".jar") || entry.endsWith(".zip")) {
                try (JarFile jar = new JarFile(file)) {
                    Enumeration<JarEntry> jarEntries = jar.entries();
                    while (jarEntries.hasMoreElements()) {
                        JarEntry je = jarEntries.nextElement();
                        String name = je.getName();
                        if (!name.endsWith(".class")) continue;
                        String className = name.replace('/', '.').replace(".class", "");
                        for (String pkg : packagesToScan) {
                            if (className.startsWith(pkg + ".")) {
                                checkAndAddClass(className, Thread.currentThread().getContextClassLoader());
                                break;
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void findClassesInDirectory(String pkg, File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                findClassesInDirectory(pkg + "." + f.getName(), f);
            } else if (f.getName().endsWith(".class")) {
                String className = pkg + "." + f.getName().substring(0, f.getName().length() - 6);
                checkAndAddClass(className, Thread.currentThread().getContextClassLoader());
            }
        }
    }

    private void checkAndAddClass(String className, ClassLoader cl) {
        try {
            Class<?> c = Class.forName(className, false, cl);
            if (!OpMode.class.isAssignableFrom(c)) return;
            if (c.isAnnotationPresent(Disabled.class)) return;
            if (c.isAnnotationPresent(TeleOp.class) || c.isAnnotationPresent(Autonomous.class)) {
                opmodes.add((OpMode) c.newInstance());
            }
        } catch (ClassNotFoundException ignored) {

        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Could not create OpMode" + e);
        }
    }
}

