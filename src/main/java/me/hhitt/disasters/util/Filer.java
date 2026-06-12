package me.hhitt.disasters.util;

import java.io.File;

public final class Filer {

    private Filer() {
    }

    public static String fixName(final String name) {
        if (name.endsWith(".yml")) {
            return name;
        }
        return name + ".yml";
    }

    public static void createFolders() {
        final File arenasFolder = new File("plugins/Disasters/Arenas");
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }
    }
}
