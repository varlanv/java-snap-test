package com.varlanv.snaptest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class SnapFile {

    final Path path;
    Content content;
    private final Queue<SnapExpected> appends;
    private final Lock writeLock;

    SnapFile(Path path, Content content) {
        this.path = path;
        this.content = content;
        this.appends = new ConcurrentLinkedQueue<>();
        this.writeLock = new ReentrantLock();
    }

    static SnapFile read(Path file) throws IOException {
        if (Files.notExists(file)) {
            throw new IllegalStateException("Snaptest file does not exist: " + file.toAbsolutePath());
        } else {
            // Read the entire file content into a single string
            var items = new TreeMap<SnapExpected.Key, List<SnapExpected>>();
            var fileContent = Files.readString(file, StandardCharsets.UTF_8);

            var currentSearchIndex = 0; // Start searching from the beginning of the file

            // The initial lines like "0.0.1" and blank lines before the first marker
            // will be skipped automatically by indexOf searching for the first valid marker.

            var versionPos = fileContent.indexOf('\n');
            var version = fileContent.substring(0, versionPos);
            while (true) {

                // Find the start of the current invocation's start marker
                var startMarkerPos = fileContent.indexOf(Constants.SNAP_START_MARKER, currentSearchIndex);
                if (startMarkerPos == -1) {
                    // No more invocations found (or this one doesn't exist)
                    break;
                }

                // Determine the actual start of the expected content.
                // This is after the fullStartMarker string AND its trailing newline sequence.
                var contentStartIndex = startMarkerPos + Constants.SNAP_START_MARKER_LEN;

                // If contentStartIndex >= fileContent.length(), it means the start marker was at the very end.

                // Find the start of the current invocation's end marker.
                // Search must begin *after* the start marker, ideally after contentStartIndex.
                var endMarkerPos = fileContent.indexOf(Constants.SNAP_START_MARKER, contentStartIndex);
                if (endMarkerPos == -1) {
                    endMarkerPos = fileContent.length() + 1;
                }

                // The expected content is the substring between the calculated contentStartIndex
                // and the beginning of the endMarker string (endMarkerPos).
                var skipSize = 1;
                var idBuilder = new StringBuilder();
                for (var i = contentStartIndex + 1; i < endMarkerPos - 1; i++) {
                    skipSize++;
                    var ch = fileContent.charAt(i);
                    if (ch == '\n') {
                        break;
                    }
                    idBuilder.append(ch);
                }
                var positionBuilder = new StringBuilder();
                for (var i = contentStartIndex + skipSize; i < endMarkerPos - 1; i++) {
                    skipSize++;
                    var ch = fileContent.charAt(i);
                    if (ch == '\n') {
                        break;
                    }
                    positionBuilder.append(ch);
                }

                var iterationInMethodBuilder = new StringBuilder();
                for (var i = contentStartIndex + skipSize; i < endMarkerPos - 1; i++) {
                    skipSize++;
                    var ch = fileContent.charAt(i);
                    if (ch == '\n') {
                        break;
                    }
                    iterationInMethodBuilder.append(ch);
                }
                var iterationTestBuilder = new StringBuilder();
                for (var i = contentStartIndex + skipSize; i < endMarkerPos - 1; i++) {
                    skipSize++;
                    var ch = fileContent.charAt(i);
                    if (ch == '\n') {
                        break;
                    }
                    iterationTestBuilder.append(ch);
                }
                var expectedValue = fileContent.substring(contentStartIndex + skipSize, endMarkerPos - 2);
                var id = idBuilder.toString();
                var position = Integer.parseInt(positionBuilder.toString());
                var iterationInMethod = Integer.parseInt(iterationInMethodBuilder.toString());
                var iterationInTest = Integer.parseInt(iterationTestBuilder.toString());
                var key = new SnapExpected.Key(id, position, iterationInMethod,iterationInTest);
                items.computeIfAbsent(key, k -> new ArrayList<>()).add(new SnapExpected(key, expectedValue));

                // Update the currentSearchIndex to look for the next start marker.
                // It should be positioned after the current end marker and its line.
                currentSearchIndex = endMarkerPos;
            }
            return new SnapFile(file, new Content(version, items));
        }
    }

    static SnapFile init(Path path, Content content) throws IOException {
        try (var bw = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            bw.write(content.version + "\n\n\n");
            for (var snapExpectedList : content.assertions.values()) {
                for (var snapExpected : snapExpectedList) {
                    bw.write(formatExpected(snapExpected));
                }
            }
            bw.flush();
            return new SnapFile(path, content);
        }
    }

    void recordAppend(SnapExpected append) {
        try {
            writeLock.lock();
            appends.add(append);
        } finally {
            writeLock.unlock();
        }
    }

    public Content save() throws IOException {
        try {
            writeLock.lock();
            if (!appends.isEmpty()) {
                var newContent = new TreeMap<>(content.assertions);
                for (var append : appends) {
                    newContent.computeIfAbsent(append.key, k -> new ArrayList<>()).add(append);
                }

                try (var bw = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING)) {
                    bw.write(content.version + "\n\n\n");
                    for (var snapExpected : newContent) {
                        bw.write(formatExpected(snapExpected));
                    }
                    bw.flush();
                }
                appends.clear();
                return new Content(content.version, content.assertions);
            } else {
                return content;
            }
        } finally {
            writeLock.unlock();
        }
    }

    private static String formatExpected(SnapExpected snapExpected) {
        return "\n" + Constants.SNAP_START_MARKER + "\n" + snapExpected.key.id + "\n" + snapExpected.key.positionInClass + "\n"
                + snapExpected.key.iterationInMethod + "\n" + snapExpected.expected + "\n";
    }

    static final class Content {

        final String version;
        final SortedMap<SnapExpected.Key, List<SnapExpected>> assertions;

        Content(String version, SortedMap<SnapExpected.Key, List<SnapExpected>> assertions) {
            this.version = version;
            this.assertions = assertions;
        }
    }
}
