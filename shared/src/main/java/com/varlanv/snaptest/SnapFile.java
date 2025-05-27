package com.varlanv.snaptest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SnapFile {

    final Path workDir;
    final String fileName;
    final Path file;
    final Map<String, List<SnapAssertion>> assertions;

    SnapFile(Path workDir, String fileName, Path file, Map<String, List<SnapAssertion>> assertions) {
        this.workDir = workDir;
        this.fileName = fileName;
        this.file = file;
        this.assertions = assertions;
    }

    static SnapFile read(Path workDir, Path fileDir, String fileName, Path file) throws IOException {
        var assertions = new HashMap<String, List<SnapAssertion>>();
        var fileExists = Files.exists(file);
        if (fileExists) {
            // Read the entire file content into a single string
            var fileContent = Files.readString(file, StandardCharsets.UTF_8);

            var currentSearchIndex = 0; // Start searching from the beginning of the file

            // The initial lines like "0.0.1" and blank lines before the first marker
            // will be skipped automatically by indexOf searching for the first valid marker.

            while (true) {
                var fullStartMarker = Constants.SNAP_START_MARKER;
                var fullEndMarker = Constants.SNAP_END_MARKER;

                // Find the start of the current invocation's start marker
                var startMarkerPos = fileContent.indexOf(fullStartMarker, currentSearchIndex);
                if (startMarkerPos == -1) {
                    // No more invocations found (or this one doesn't exist)
                    break;
                }

                // Determine the actual start of the expected content.
                // This is after the fullStartMarker string AND its trailing newline sequence.
                var contentStartIndex = startMarkerPos + fullStartMarker.length();

                // If contentStartIndex >= fileContent.length(), it means the start marker was at the very end.

                // Find the start of the current invocation's end marker.
                // Search must begin *after* the start marker, ideally after contentStartIndex.
                var endMarkerPos = fileContent.indexOf(fullEndMarker, contentStartIndex);
                if (endMarkerPos == -1) {
                    throw new IOException("Malformed snapshot file: missing end marker "
                            + "(start marker found at position " + startMarkerPos + ").");
                }

                // The expected content is the substring between the calculated contentStartIndex
                // and the beginning of the endMarker string (endMarkerPos).
                var skipSize = 1;
                var uniqueIdBuilder = new StringBuilder();
                for (var i = contentStartIndex + 1; i < endMarkerPos - 1; i++) {
                    skipSize++;
                    var ch = fileContent.charAt(i);
                    if (ch == '\n') {
                        break;
                    }
                    uniqueIdBuilder.append(ch);
                }
                var expectedValue = fileContent.substring(contentStartIndex + skipSize, endMarkerPos - 1);
                var uniqueId = uniqueIdBuilder.toString();
                assertions
                        .computeIfAbsent(uniqueId, k -> new ArrayList<>())
                        .add(new SnapAssertion(uniqueId, expectedValue));
                ;

                // Update the currentSearchIndex to look for the next start marker.
                // It should be positioned after the current end marker and its line.
                currentSearchIndex = endMarkerPos + fullEndMarker.length();
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
        }
        return new SnapFile(workDir, fileName, file, assertions);
    }

    void append(String actual, String uniqueId, int count) throws Exception {
        var actualWithMarkers = "\n" + Constants.SNAP_START_MARKER + "\n" + uniqueId + "\n" + actual + "\n"
                + Constants.SNAP_END_MARKER + "\n";
        Files.writeString(file, actualWithMarkers, StandardOpenOption.APPEND);
    }
}
