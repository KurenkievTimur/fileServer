package com.kurenkievtimur.server;

import com.kurenkievtimur.common.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServerFileMap implements Serializable {
    private static ServerFileMap serverFileMap;
    private final Map<Integer, String> fileMap = new HashMap<>();
    private int fileMapId = 0;

    private ServerFileMap() {
    }

    public static ServerFileMap getServerFileMap() {
        if (serverFileMap == null) {
            serverFileMap = new ServerFileMap();
            try {
                serverFileMap = (ServerFileMap) Utils.deserialize(Utils.getServerFilePath("file_map.data"));
            } catch (IOException | ClassNotFoundException ignored) {
            }
        }

        return serverFileMap;
    }

    public int put(String name) {
        fileMap.put(++fileMapId, name);
        return fileMapId;
    }

    public String get(Integer key) {
        return fileMap.get(key);
    }

    public void delete(String value) {
        for (Map.Entry<Integer, String> entry : fileMap.entrySet()) {
            if (entry.getValue().equals(value)) {
                fileMap.remove(entry.getKey());
                break;
            }
        }
    }
}
