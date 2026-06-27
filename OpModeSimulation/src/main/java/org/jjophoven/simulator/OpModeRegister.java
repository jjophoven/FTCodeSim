package org.jjophoven.simulator;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class OpModeRegister {
    Set<OpMode> autos = new LinkedHashSet<>();
    Set<OpMode> teleops = new LinkedHashSet<>();

    public OpModeRegister() {
        findAnnotatedOpModes();
    }

    public Set<OpMode> getAutonomousModes() {
        return autos;
    }

    public Set<OpMode> getTeleOpModes() {
        return teleops;
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
            if (c.isAnnotationPresent(TeleOp.class)) {
                teleops.add((OpMode) c.newInstance());
            } else if (c.isAnnotationPresent(Autonomous.class)) {
                autos.add((OpMode) c.newInstance());
            }
        } catch (ClassNotFoundException ignored) {

        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Could not create OpMode" + e);
        }
    }
}

