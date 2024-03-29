package io.jsrminer.evaluation;

import io.jsrminer.sourcetree.SourceLocation;

class RefactoringMatcher {
    static boolean isMatch(Ref rd, Ref rm) {
        boolean equalBeforeName = rd.localNameBefore.equals(rm.localNameBefore);
        boolean equalAfterName = rd.localNameAfter.equals(rm.localNameAfter);

        if (rd.refType.name().toLowerCase().contains("file")) {
            return rd.commitId.equals(rm.commitId)
                    && rm.refType.equals(rd.refType)
                    && equalBeforeName
                    && equalAfterName;
        }

        boolean equalBeforeLocation = equalLocation(rd.getLocationBefore(), rm.getLocationBefore());
        boolean equalAfterLocation = equalLocation(rd.getLocationAfter(), rm.getLocationAfter());

        return rd.commitId.equals(rm.commitId)
                && rm.refType.equals(rd.refType)
                && equalBeforeLocation
                && equalBeforeName
                && equalAfterLocation
                && equalAfterName;
    }

    private static boolean equalLocation(SourceLocation rdLocation, SourceLocation rmLocation) {
        return rdLocation.start == rmLocation.start && rdLocation.end == rmLocation.end
                && rdLocation.getFilePath().equals(rmLocation.getFilePath());
    }
}
