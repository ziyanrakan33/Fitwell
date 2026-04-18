package fitwell.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    public static String readTextFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
