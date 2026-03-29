package de.idiotischer.bob.util;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

//TODO: placeholder aus github asets fetchen
public class FileUtil {

    public static Path getJarDir() {
        try {
            return Path.of(
                FileUtil.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            ).getParent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a writable data directory for the application.
     *
     * When running inside an AppImage the filesystem is read-only, so we follow
     * the XDG Base Directory spec and write to ~/.local/share/BOB/ instead.
     * The AppImage runtime always sets the APPIMAGE environment variable, which
     * we use as the detection signal.
     *
     * In all other cases (plain JAR, IDE, Windows) we keep the original
     * behaviour and use the directory next to the JAR.
     */
    public static Path getDataDir() {
        boolean insideAppImage = System.getenv("APPIMAGE") != null;

        if (!insideAppImage) {
            // Plain JAR / IDE / Windows original behaviour
            return getJarDir().toAbsolutePath();
        }

        // Running inside AppImage, filesystem is read-only, use XDG dir
        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        Path base = (xdgDataHome != null && !xdgDataHome.isBlank())
            ? Path.of(xdgDataHome)
            : Path.of(System.getProperty("user.home"), ".local", "share");

        Path dataDir = base.resolve("BOB");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new RuntimeException(
                "Could not create data directory: " + dataDir,
                e
            );
        }
        return dataDir;
    }

    public static CompletableFuture<Void> replaceIfNotExistingAsync(
        ClassLoader resourceRoot
    ) {
        return CompletableFuture.runAsync(() ->
            replaceIfNotExisting(resourceRoot)
        );
    }

    private static void replaceIfNotExisting(ClassLoader resourceRoot) {
        try {
            extractFolder(resourceRoot, "/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void extractFolder(
        ClassLoader resourceRoot,
        @NotNull String folderName
    ) throws Exception {
        ClassPath classPath = ClassPath.from(resourceRoot);

        //TODO wenn internet zugang dann von github assets holen

        String prefix = folderName.startsWith("/")
            ? folderName.substring(1)
            : folderName;
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix += "/";
        }

        for (ClassPath.ResourceInfo resource : classPath.getResources()) {
            String resourceName = resource.getResourceName();

            if (
                resourceName.startsWith(prefix) &&
                !resourceName.endsWith(".class") &&
                !resourceName.startsWith("META-INF/")
            ) {
                // Extract to the writable data directory, not next to the JAR
                Path destination = getDataDir().resolve(resourceName);

                try {
                    if (Files.notExists(destination)) {
                        if (destination.getParent() != null) {
                            Files.createDirectories(destination.getParent());
                        }

                        try (InputStream is = resource.url().openStream()) {
                            Files.copy(
                                is,
                                destination,
                                StandardCopyOption.REPLACE_EXISTING
                            );
                            System.out.println("Extracted: " + resourceName);
                        }
                    }
                } catch (Exception e) {
                    System.err.println(
                        "Failed to extract " +
                            resourceName +
                            ": " +
                            e.getMessage()
                    );
                }
            }
        }
    }

    @ApiStatus.Obsolete
    public static Path getRunningDir() {
        return getDataDir();
    }

    public static Path getConfigDir() {
        Path configDir = getDataDir().resolve("config/");

        if (Files.notExists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return configDir;
    }

    public static Path getScenarioDir() {
        Path scenarioDir = getDataDir().resolve("scenario/");

        if (Files.notExists(scenarioDir)) {
            try {
                Files.createDirectories(scenarioDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return scenarioDir;
    }

    public static Path getScenarioDir(String scenarioName) {
        Path scenarioDir = getScenarioDir().resolve(scenarioName);

        if (Files.notExists(scenarioDir)) {
            try {
                Files.createDirectories(scenarioDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return scenarioDir;
    }

    public static List<Path> getAllScenarios() {
        Path scenarioDir = getScenarioDir();

        if (!Files.isDirectory(scenarioDir)) return List.of();

        try (Stream<Path> stream = Files.list(scenarioDir)) {
            return stream.filter(Files::isDirectory).toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to list scenarios", e);
        }
    }

    public static Path getDefaultScenarioDir() {
        Path scenarioDir = getScenarioDir().resolve("default/");

        if (Files.notExists(scenarioDir)) {
            try {
                Files.createDirectories(scenarioDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return scenarioDir;
    }

    public static Path getIconPath() {
        Path iconsDir = getDataDir();

        //TODO: check this ig
        if (Files.notExists(iconsDir)) {
            try {
                Files.createDirectories(iconsDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return iconsDir;
    }

    public static Path getDefaultConfig() {
        Path configPath = getConfigDir().resolve("config.json");

        if (Files.notExists(configPath)) {
            try {
                Files.createFile(configPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return configPath;
    }

    public static Path getHostConfig() {
        Path hostConfig = getConfigDir().resolve("host.json");

        if (Files.notExists(hostConfig)) {
            try {
                Files.createFile(hostConfig);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return hostConfig;
    }

    public static Path getFlagsDir() {
        Path flagsDir = getDataDir().resolve("flags/");

        if (Files.notExists(flagsDir)) {
            try {
                Files.createDirectories(flagsDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return flagsDir;
    }

    public static Path getCoatsDir() {
        Path iconsDir = getDataDir().resolve("icons/");

        if (Files.notExists(iconsDir)) {
            try {
                Files.createDirectories(iconsDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return iconsDir;
    }

    /* WIP */
    @ApiStatus.Experimental
    public static Path getCountryFlagsDir(String abbreviation) {
        Path countryFlagsDir = getFlagsDir().resolve(abbreviation);

        if (Files.notExists(countryFlagsDir)) {
            try {
                Files.createDirectories(countryFlagsDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return countryFlagsDir;
    }

    /* WIP */
    @ApiStatus.Experimental
    public static Path getCoat(String abbreviation) {
        Path countryFlagsDir = getCoatsDir().resolve(abbreviation);
        return countryFlagsDir.resolve(abbreviation + ".png");
    }

    /* WIP */
    @ApiStatus.Experimental
    public static Path getCoat(String abbreviation, String flagAbbreviation) {
        Path countryFlagsDir = getFlagsDir().resolve(abbreviation);
        Path flag = countryFlagsDir.resolve(
            abbreviation + "_" + flagAbbreviation + ".png"
        );

        if (Files.notExists(flag)) {
            flag = getFlag(abbreviation);
        }

        return flag;
    }

    public static Path getFlag(String abbreviation) {
        Path countryFlagsDir = getFlagsDir().resolve(abbreviation);
        return countryFlagsDir.resolve(abbreviation + ".png");
    }

    //flagAbbreviation can also just be any other thing not only ideology yk
    public static Path getFlag(String abbreviation, String flagAbbreviation) {
        Path countryFlagsDir = getFlagsDir().resolve(abbreviation);
        Path flag = countryFlagsDir.resolve(
            abbreviation + "_" + flagAbbreviation + ".png"
        );

        if (Files.notExists(flag)) {
            flag = getFlag(abbreviation);
        }

        return flag;
    }
}
