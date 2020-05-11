package io.jsrminer.io;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.WeakHashMap;

public class FileUtil {

    private static Map<Path, Path> realPathCache = new WeakHashMap<>();

    /**
     * Returns the current working directory.
     */
    public static Path getWorkingDirectory() {
        return Paths.get("");
    }

    /**
     * Makes a relative Path that is relative to the working directory.
     */
    public static Path getRelativeToWorkingDirectory(Path path) {
        return getRelativeTo(getWorkingDirectory(), path);
    }

    /**
     * Makes a relative Path that is relative to the 'from' directory.
     */
    public static Path getRelativeTo(Path from, Path to) {
        return toRealPath(from).normalize().relativize(toRealPath(to)).normalize();
    }


    public static Path toRealPath(Path p) {
        return realPathCache.computeIfAbsent(p, k -> {
            try {
                return p.toRealPath();
            } catch (NoSuchFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return k;
        });
    }

    public static void clearCache() {
        realPathCache.clear();
    }
}
