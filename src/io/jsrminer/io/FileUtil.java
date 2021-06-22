package io.jsrminer.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

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

    public static String getMemberExtension(String filename) {
        int dotIndex = filename.indexOf(".");

        if (dotIndex >= 0) {
            return filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        }
        return null;
    }

    public static String getExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }

    public static void clearCache() {
        realPathCache.clear();
    }

    public static void download(final URL sourceUrl, final File destination) throws IOException {
        FileUtils.copyURLToFile(sourceUrl, destination);
    }

    public static void unZip(File zipFilePath) throws IOException {
    /*    java.util.zip.ZipFile zipFile = new ZipFile(zipFilePath);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(projectFolder.getParentFile(), entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    in.close();
                    out.close();
                }
            }
        } finally {
            zipFile.close();
        }*/
    }

    public static String getResourcePath(String resourceName) {
        return FileUtil.class.getClassLoader().getResource(resourceName).getFile();
    }

    public static String readFileContent(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void allDirectoriesInPath(String filepath, Set<String> directories) {
        String name = FilenameUtils.getName(filepath);
        String dir = filepath.substring(0, filepath.length() - name.length());
        if (dir.length() > 0) {
            dir = dir.substring(0, dir.length() - 1);
            directories.add(dir);
            allDirectoriesInPath(dir, directories);
        }
    }

    public static String getFolder(String filepath) {
        String name = FilenameUtils.getName(filepath);
        String dir = filepath.substring(0, filepath.length() - name.length());
        if (dir.length() > 0) {
            dir = dir.substring(0, dir.length() - 1);

            return dir;
        }

        return "";
    }
}
