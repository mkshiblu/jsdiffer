package io.jsrminer.evaluation;

import java.util.*;

public class CommitRefactoringsDiff {

    private String commitId;

    public CommitRefactoringsDiff(String commitId) {
        this.commitId = commitId;
    }

    private Map<Ref.RefType, List<Ref>> completelyUnmatchedTypeRefsInRd = new LinkedHashMap<>();

    private List<Ref> allMatched = new ArrayList<>();
    private List<Ref> allUnmatchedInRds = new ArrayList<>();

    private Map<Ref.RefType, List<Ref>> unmatchedRdsRefTypeMap = new LinkedHashMap<>();
    private Map<Ref.RefType, List<RmRow>> unmatchedRms = new LinkedHashMap<>();
    private Map<Ref.RefType, List<Ref>> matchedRefTypeMap = new LinkedHashMap<>();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.commitId);
        builder.append("\t");

        if (completelyUnmatchedTypeRefsInRd.size() > 0) {
            builder.append("Missed Refactorings Type in RD: ");
            builder.append(completelyUnmatchedTypeRefsInRd.keySet().toString());
        }
//
        builder.append(System.lineSeparator());
        builder.append(getUnmatchedInRdCount() + " Unmatched in Rd-lines: ");
        var missedLines = getAllUnmatchedInRds().stream().map(rdRow -> rdRow.lineNo).toArray(Integer[]::new);
        builder.append(Arrays.toString(missedLines));

        builder.append(System.lineSeparator());
        builder.append(getTotalMatchedCount() + " Matched-lines: ");
        var matchedLines = getAllMatched().stream().map(rdRow -> rdRow.lineNo).toArray(Integer[]::new);
        builder.append(Arrays.toString(matchedLines));

        return builder.toString();
    }

    public int getTotalUnmatchedCount() {
        return getUnmatchedInRdCount() + getUnmatchedInRmCount();
    }

    public int getUnmatchedInRdCount() {
        return allUnmatchedInRds.size();
    }

    public List<Ref> getAllUnmatchedInRds() {
        return allUnmatchedInRds;
    }

    public List<Ref> getAllMatched() {
        return allMatched;
    }

    public int getUnmatchedInRmCount() {
        return unmatchedRms.values().stream().mapToInt(List::size).sum();
    }

    public int getTotalMatchedCount() {
        return matchedRefTypeMap.values().stream().mapToInt(List::size).sum();
    }

    void registerUnmatchedInRd(Ref rdRef) {
        allUnmatchedInRds.add(rdRef);
        this.unmatchedRdsRefTypeMap.computeIfAbsent(rdRef.refType, key -> new ArrayList<>()).add(rdRef);
    }

    void registerUnmatchedInRm(Ref ref) {

    }

    void registerMatched(Ref ref) {
        this.allMatched.add(ref);
        this.matchedRefTypeMap.computeIfAbsent(ref.refType, key -> new ArrayList<>()).add(ref);
    }

    void registerCompletelyUnmatchedTypeRefsInRd(Ref.RefType refType, List<Ref> rdRefactorings) {
        this.completelyUnmatchedTypeRefsInRd.put(refType, rdRefactorings);
    }

    public String getCommitId() {
        return commitId;
    }

    public Map<Ref.RefType, List<Ref>> getCompletelyUnmatchedTypeRefsInRd() {
        return completelyUnmatchedTypeRefsInRd;
    }

    public Map<Ref.RefType, List<Ref>> getUnmatchedRdsRefTypeMap() {
        return unmatchedRdsRefTypeMap;
    }

    public Map<Ref.RefType, List<RmRow>> getUnmatchedRms() {
        return unmatchedRms;
    }
}
