package dev.equo.swt;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Runs a short query command and returns its trimmed stdout, or null when it cannot be run. Bounded
 * by a timeout because callers sit on the Display initialization path; stderr is discarded so loader
 * noise never reaches the caller.
 */
final class Commands {
    private Commands() {
    }

    static String run(String... command) {
        Process process = null;
        try {
            process = new ProcessBuilder(command).redirectErrorStream(false).start();
            String output;
            try (InputStream in = process.getInputStream()) {
                output = new String(Java8.readAllBytes(in), StandardCharsets.UTF_8).trim();
            }
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                return null;
            }
            return output;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
