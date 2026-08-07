package org.codeblooded.ftcodesim.ascope;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.codeblooded.ftcodesim.ascope.json.JsonEditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Objects;

public class AdvantageScopeRunner extends JsonEditor {
    ArrayNode sources;
    Process window;

    public static AdvantageScopeRunner INSTANCE;

    public AdvantageScopeRunner(SeasonField seasonField) {
        super(getStateFile());
        INSTANCE = this;
        sources = fieldView3D(seasonField);

        URL robotModelsUrl = Objects.requireNonNull(
                getClass().getClassLoader().getResource("assets/robot-models"));

        Path userAssets = new File(getAdvantageScopeFolder(), "userAssets").toPath();

        try {
            copyRobotModels(robotModelsUrl, userAssets);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        JsonEditor prefsEditor = new JsonEditor(new File(getAdvantageScopeFolder(), "prefs.json"));
        prefsEditor.root.put("liveMode", "rlog");

        for (int i = 0; i < seasonField.gamePieces.length; i++) {
            seasonField.gamePieces[i].log(String.valueOf(i));
        }

        saveConfig();

        if (isAdvantageScopeRunning()) {
            return;
        }

        File exe = new File(
                System.getenv("LOCALAPPDATA"),
                "Programs/advantagescope/AdvantageScope.exe"
        );

        try {
            window = new ProcessBuilder(exe.getAbsolutePath()).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyRobotModels(URL robotModelsUrl, Path userAssets) throws IOException, URISyntaxException {
        if ("jar".equals(robotModelsUrl.getProtocol())) {
            String uri = robotModelsUrl.toURI().toString();
            String[] parts = uri.split("!", 2);

            try (FileSystem fileSystem = FileSystems.newFileSystem(
                    java.net.URI.create(parts[0]),
                    Collections.emptyMap()
            )) {
                copyRobotModelFolders(fileSystem.getPath(parts[1]), userAssets);
            }
        } else {
            copyRobotModelFolders(Paths.get(robotModelsUrl.toURI()), userAssets);
        }
    }

    private static void copyRobotModelFolders(Path robotModels, Path userAssets) throws IOException {
        Files.createDirectories(userAssets);

        try (java.util.stream.Stream<Path> paths = Files.list(robotModels)) {
            paths.filter(Files::isDirectory)
                    .forEach(path -> copyFolder(path, userAssets));
        }
    }

    private static Process getAdvantageScopeProcess() {
        try {
            Process process = new ProcessBuilder(
                    "pgrep",
                    "-f",
                    "AdvantageScope"
            ).start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                if (reader.readLine() != null) {
                    return process;
                }
            }

        } catch (IOException ignored) {
        }

        return null;
    }

    private static boolean isAdvantageScopeRunning() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            Process process;

            if (os.contains("win")) {
                process = new ProcessBuilder("tasklist").start();
            } else if (os.contains("mac")) {
                process = new ProcessBuilder("pgrep", "-f", "AdvantageScope").start();
            } else {
                process = new ProcessBuilder("pgrep", "-f", "AdvantageScope").start();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("advantagescope")) {
                        return true;
                    }
                }
            }

        } catch (IOException ignored) {
        }

        return false;
    }

    public boolean isOpen() {
        return window == null || window.isAlive();
    }

    public ArrayNode fieldView3D(SeasonField seasonField) {
        ObjectNode tabs = (ObjectNode) get("hubs/0/state/tabs");

        tabs.put("selected", 1);

        ArrayNode tabArray =
                (ArrayNode) tabs.get("tabs");

        for(JsonNode tab : tabArray) {
            if(
                    isEqualTo(tab, "controller/game", seasonField.ascopeName) &&
                    isEqualTo(tab, "title", "3D FTCodeSim")
            ) {
                return (ArrayNode) get(tab, "controller/sources");
            }
        }

        System.out.println("adding 3d field");

        ObjectNode tab = mapper.createObjectNode();

        tab.put(
                "type",
                3
        );
        tab
                .put("title", "3D FTCodeSim")
                .put("controllerUUID", "abcdefghijklmnopqrstuv1234567890");
        tab.set("controller", mapper.createObjectNode().put("game", seasonField.ascopeName).set("sources", mapper.createArrayNode()));
        //tab.set("renderer", mapper.createObjectNode());
        tab.put("controlsHeight", 100);
        tabArray.insert(1, tab);

        return (ArrayNode) get(tab, "controller/sources");
    }

    public void addSource(
            String key,
            SourceType sourceType
    ) {
        for (JsonNode source : sources) {
            String currentModel = sourceType.options.get("model") != null ? sourceType.options.get("model").asText() : "";
            String currentVariant = sourceType.options.get("variant") != null ? sourceType.options.get("variant").asText() : "";

            boolean isSameRobotModel = (sourceType.options.get("model") != null
                    && isEqualTo(source, "options/model", currentModel));
            boolean isSameVariant = (sourceType.options.get("variant") != null
                    && isEqualTo(source, "options/variant", currentVariant));

            if (isEqualTo(source, "logKey", key) && (isSameRobotModel || isSameVariant)
            ) {
                //System.out.println("Source already exists: " + key);
                return;
            }
        }

        System.out.println("Adding source: " + key);

        ObjectNode source =
                mapper.createObjectNode();

        source
                .put("type", sourceType.type)
                .put("logKey", key)
                .put("logType", sourceType.logType)
                .put("visible", true)
                .set("options", sourceType.options);

        sources.add(source);
    }

    private static File getStateFile() {
        File folder = getAdvantageScopeFolder();

        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Could not find AdvantageScope");
        }

        File[] files = folder.listFiles((dir, name) ->
                name.startsWith("state-") && name.endsWith(".json"));

        if (files == null || files.length == 0) {
            throw new RuntimeException("No AdvantageScope state file found");
        }

        return files[0];
    }

    private static File getAdvantageScopeFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return new File(appData, "AdvantageScope");
            }
            return new File(new File(new File(home, "AppData"), "Roaming"), "AdvantageScope");
        } else if (os.contains("mac")) {
            return new File(new File(new File(home, "Library"), "Application Support"), "AdvantageScope");
        } else {
            String xdg = System.getenv("XDG_CONFIG_HOME");
            if (xdg != null) {
                return new File(xdg, "AdvantageScope");
            }
            return new File(new File(home, ".config"), "AdvantageScope");
        }
    }

    public static void copyFolder(Path source, Path destination) {
        Path sourceFileName = source.getFileName();
        if (sourceFileName == null) {
            throw new IllegalArgumentException("Source path has no file name: " + source);
        }

        Path targetRoot = destination.resolve(sourceFileName.toString());

        // Folder already exists, do nothing
        if (Files.exists(targetRoot)) {
            return;
        }

        System.out.println("Copying " + source + " to " + targetRoot);

        try {
            Files.walk(source).forEach(path -> {
                try {
                    Path relativePath = source.relativize(path);
                    Path target = targetRoot.resolve(relativePath.toString());

                    if (Files.isDirectory(path)) {
                        Files.createDirectories(target);
                    } else {
                        Path parent = target.getParent();
                        if (parent != null) {
                            Files.createDirectories(parent);
                        }

                        Files.copy(path, target);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            System.out.println("Working directory: " + System.getProperty("user.dir"));
            throw new RuntimeException(e);
        }
    }
}