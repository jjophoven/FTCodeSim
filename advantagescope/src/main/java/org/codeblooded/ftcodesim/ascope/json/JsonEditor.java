package org.codeblooded.ftcodesim.ascope.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class JsonEditor {
    public final ObjectMapper mapper;
    public final ObjectNode root;
    public final File file;

    public JsonEditor(File file) {
        mapper = new ObjectMapper();
        try {
            root = (ObjectNode) mapper.readTree(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.file = file;
    }

    public boolean isEqualTo(JsonNode node, String key, String value) {
        JsonNode result = get(node, key);
        return (result != null && result.asText().equals(value));
    }

    public JsonNode get(JsonNode node, String masterKey) {
        String[] keys = masterKey.split("/");
        for (String key : keys) {
            if (node instanceof ArrayNode) {
                node = node.get(Integer.parseInt(key));
                continue;
            }
            if (node.get(key) != null) {
                node = node.get(key);
            } else {
                return null;
            }
        }
        return node;
    }

    public JsonNode get(String masterKey) {
       return get(root, masterKey);
    }

    public void saveConfig() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(
                            file,
                            root
                    );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
