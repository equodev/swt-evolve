package dev.equo.swt.bench;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Writes each bench shape's serialized J&rarr;D payload to disk so the client-side decode cost can
 * be measured against the exact bytes Java sends, rather than a synthetic stand-in.
 *
 * <p>Output directory is {@code bench.dumpDir}, default {@code build/bench-payloads}.
 */
@DisabledOnOs(OS.LINUX)
@Tag("bench")
public class DumpBenchPayloadsTest {

    @Test
    void dumpPayloads() throws IOException {
        Path dir = Paths.get(System.getProperty("bench.dumpDir", "build/bench-payloads"));
        Files.createDirectories(dir);
        for (BenchPayloads.Shape s : List.of(BenchPayloads.SMALL, BenchPayloads.MEDIUM,
                BenchPayloads.LARGE, BenchPayloads.IMAGE_SMALL, BenchPayloads.IMAGE_LARGE,
                BenchPayloads.SVG, BenchPayloads.WORKBENCH)) {
            byte[] bytes = s.serialize();
            Path out = dir.resolve(s.name + ".json");
            Files.write(out, bytes);
            System.out.println("[dump] " + out.toAbsolutePath() + " = " + bytes.length + " bytes");
        }
    }
}
