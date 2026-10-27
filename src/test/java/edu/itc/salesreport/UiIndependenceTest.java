package edu.itc.salesreport;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class UiIndependenceTest {
    private static final List<String> PACKAGES = List.of("model", "ingest", "engine", "ui");
    private static final List<String> BANNED =
            List.of("import javafx.", "import java.awt.", "import javax.swing.");

    @Test
    void coreHasNoToolkitImports() throws IOException {
        Path base = Path.of("src/main/java/edu/itc/salesreport");
        var violations = new ArrayList<String>();
        for (String pkg : PACKAGES) {
            try (var files = Files.list(base.resolve(pkg))) {
                for (Path f : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                    for (String line : Files.readAllLines(f)) {
                        if (BANNED.stream().anyMatch(line.strip()::startsWith)) {
                            violations.add(f + ": " + line);
                        }
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), () -> "Toolkit imports found: " + violations);
    }
}