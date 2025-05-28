package com.varlanv.snaptest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class SnapFile {

    final Path path;
    final Content content;
    private final Lock writeLock = new ReentrantLock();

    SnapFile(Path path, Content content) {
        this.path = path;
        this.content = content;
    }

    static SnapFile read(Path file) throws IOException {
        if (Files.notExists(file)) {
            throw new IllegalStateException("Snaptest file does not exist: " + file.toAbsolutePath());
        } else {
            // Read the entire file content into a single string
            var items = new HashMap<String, List<SnapExpected>>();
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
                    endMarkerPos = fileContent.length();
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
                var expectedValue = fileContent.substring(contentStartIndex + skipSize, endMarkerPos - 1);
                var id = idBuilder.toString();
                var position = Integer.parseInt(positionBuilder.toString());
                items.computeIfAbsent(id, k -> new ArrayList<>()).add(new SnapExpected(id, expectedValue, position));

                // Update the currentSearchIndex to look for the next start marker.
                // It should be positioned after the current end marker and its line.
                currentSearchIndex = endMarkerPos + Constants.SNAP_START_MARKER_LEN;
                if (currentSearchIndex < fileContent.length()) {
                    char firstCharAfterEndMarker = fileContent.charAt(currentSearchIndex);
                    if (firstCharAfterEndMarker == '\r'
                            && currentSearchIndex + 1 < fileContent.length()
                            && fileContent.charAt(currentSearchIndex + 1) == '\n') {
                        currentSearchIndex += 2; // Skip CRLF
                    } else if (firstCharAfterEndMarker == '\n' || firstCharAfterEndMarker == '\r') {
                        currentSearchIndex += 1; // Skip LF or CR
                    }
                }
            }
            return new SnapFile(file, new Content(version, items));
        }
    }

    static SnapFile init(Path path, Content content) throws IOException {
        try (var bw = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            bw.write(content.version + "\n\n\n");
            var sorted = new TreeSet<SnapExpected>(Comparator.comparingInt(it -> it.position));
            content.assertions.forEach((ignore, value) -> sorted.addAll(value));
            for (var snapExpected : sorted) {
                bw.write("\n" + Constants.SNAP_START_MARKER + "\n" + snapExpected.id + "\n" + snapExpected.position
                        + "\n" + snapExpected.expected + "\n");
            }
            bw.flush();
        }
        return new SnapFile(path, content);
    }

    void recordAppend(String actual, String id, int position) {
        //        appends.add(new Append(actual, id, position));
    }

    public void save(Iterable<SnapExpected> items) throws IOException {
        //        try {
        //            writeLock.lock();
        //            if (!appends.isEmpty()) {
        //                var sortedAppends = new TreeSet<SnapFile.Append>(Comparator.comparingInt(it -> it.position));
        //                sortedAppends.addAll(currentAppends.get());
        //                sortedAppends.addAll(appends);
        //                                var actualWithMarkers = "\n" + Constants.SNAP_START_MARKER + "\n" + uniqueId +
        // "\n" +
        //                 actual + "\n";
        //                                Files.writeString(file, actualWithMarkers, StandardOpenOption.APPEND);
        //            }
        //        } finally {
        //            writeLock.unlock();
        //        }
    }

    static final class Append {

        final String actual;
        final String id;
        final int position;

        Append(String actual, String id, int position) {
            this.actual = actual;
            this.id = id;
            this.position = position;
        }
    }

    static final class Content {

        final String version;
        final Map<String, List<SnapExpected>> assertions;

        Content(String version, Map<String, List<SnapExpected>> assertions) {
            this.version = version;
            this.assertions = assertions;
        }
    }
}
