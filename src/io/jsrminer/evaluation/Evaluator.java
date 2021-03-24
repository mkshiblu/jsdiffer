package io.jsrminer.evaluation;

import io.jsrminer.sourcetree.SourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Evaluator {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    final String rdEvaluationFile = "resources\\evaluation\\rd_eval.txt";
    final String rmEvaluationFile = "resources\\evaluation\\rm_eval_data.txt";
    EvaluationDataSet<RdRow> rdDataSet = new EvaluationDataSet<>();
    EvaluationDataSet<RmRow> rmDataSet = new EvaluationDataSet<>();

    public Evaluator() {
    }

    public static void main(String[] args) {
        new Evaluator().evaluateAll();
    }

    public void evaluateAll() {
        DataSetDiff diff = new DataSetDiff();
        loadDatasets();
        for (var projectEntry :
                rdDataSet.projectCommitsMap.entrySet()) {
            var project = projectEntry.getKey();
            var commitMap = projectEntry.getValue();
            var rmProjectMap = rmDataSet.projectCommitsMap.get(project);

            if (rmProjectMap == null) {
                //not found
                diff.registerProjectNotReportedByRm(project);
            } else {
                for (var commitEntry : commitMap.entrySet()) {
                    var rdCommit = commitEntry.getKey();
                    var rmCommitRefList = rmProjectMap.get(rdCommit);
                    if (rmCommitRefList == null) {
                        // not found
                        diff.registerCommitNotReportedByRm(project, rdCommit);
                    } else {
                        var commitDiff = diff(project, rdCommit);
                        diff.registerCommitDiff(commitDiff);
                    }
                }
            }
        }

        System.out.println(diff.toString());
    }

    public void evaluate(String project, String commit) {
        loadDatasets();
        diff(project, commit);
    }

    public void loadDatasets() {
        loadRdData(this.rdEvaluationFile);
        loadRmData(this.rmEvaluationFile);
    }

    public CommitRefactoringsDiff diff(String project, String commit) {
        var rdRefs = rdDataSet.projectCommitsMap
                .get(project)
                .get(commit);
        var rmRefs = rmDataSet.getRefsInCommit(commit);
        var result = diffCommitRefs(rdRefs, rmRefs);
        return result;
    }

    private CommitRefactoringsDiff diffCommitRefs(List<RdRow> rdRefs, List<RmRow> rmRefs) {

        boolean matchFound;

        var rdRefTypeMap = new HashMap<Ref.RefType, List<RdRow>>();
        var rmRefTypeMap = new HashMap<Ref.RefType, List<RmRow>>();

        for (var rdRow : rdRefs) {
            rdRefTypeMap
                    .computeIfAbsent(rdRow.refType, key -> new ArrayList<>())
                    .add(rdRow);
        }

        for (var ref : rmRefs) {
            rmRefTypeMap
                    .computeIfAbsent(ref.refType, key -> new ArrayList<>())
                    .add(ref);
        }

        CommitRefactoringsDiff result = diffRefTypeMaps(rdRefTypeMap, rmRefTypeMap);

        return result;
    }

    private CommitRefactoringsDiff diffRefTypeMaps(Map<Ref.RefType, List<RdRow>> rdRefTypeMap, Map<Ref.RefType, List<RmRow>> rmRefTypeMap) {
        var commitID = rdRefTypeMap.values().stream().findAny().get().get(0).commit;
        CommitRefactoringsDiff result = new CommitRefactoringsDiff(commitID);

        for (var rdEntry : rdRefTypeMap.entrySet()) {
            var refType = rdEntry.getKey();
            var rdRefactorings = rdRefTypeMap.get(refType);
            var rmRefactorings = rmRefTypeMap.get(refType);

            if (rmRefactorings != null) {
                matchSameTypeRefactorings(rdRefactorings, rmRefactorings, result);
            } else {
                result.registerCompletelyUnmatchedTypeRefsInRd(refType, rdRefactorings);
            }
        }

        return result;
    }

    private void matchSameTypeRefactorings(List<RdRow> rdRefactorings, List<RmRow> rmRefactorings, CommitRefactoringsDiff result) {
        boolean matchFound;
        RdRow rdRef;
        RmRow rmRef;

        for (ListIterator<RdRow> rdIterator = rdRefactorings.listIterator(); rdIterator.hasNext(); ) {
            matchFound = false;
            rdRef = rdIterator.next();

            for (ListIterator<RmRow> rmIterator = rmRefactorings.listIterator(); rmIterator.hasNext(); ) {
                rmRef = rmIterator.next();
                if (isMatch(rdRef, rmRef)) {
                    matchFound = true;
                    rmIterator.remove();
                    rdIterator.remove();
                    break;
                }
            }

            if (matchFound) {
                result.registerMatched(rdRef);
            } else {
                result.registerUnmatchedInRd(rdRef);
            }
        }
    }

    private void loadRdData(String filePath) {
        try {
            var lines = Files.readAllLines(Paths.get(filePath));
            for (int i = 1; i < lines.size(); i++) {
                var row = processRdRow(lines.get(i), i);
                rdDataSet.reportRow(row);
            }

        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    private void loadRmData(String filePath) {
        try {
            var lines = Files.readAllLines(Paths.get(filePath));
            for (int i = 1; i < lines.size(); i++) {
                var row = parseRmRefactoring(lines.get(i), i);
                rmDataSet.reportRow(row);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    private RmRow parseRmRefactoring(String line, int lineNo) {
        var row = new RmRow();
        row.lineNo = lineNo;
        var tokens = line.split("\t");
        row.repository = tokens[0];
        row.commit = tokens[1];
        row.refType = toRefType(tokens[2].replaceAll("(?i)method", "function"));
        row.setLocationBefore(tokens[3]);
        row.localNameBefore = tokens[4];
        row.setLocationAfter(tokens[5]);
        row.localNameAfter = tokens[6];
        return row;
    }

    private RdRow processRdRow(String line, int lineNo) {
        var row = new RdRow();
        row.lineNo = lineNo;
        var tokens = line.split("\t");

        row.repository = tokens[0];
        row.commit = tokens[1];
        row.nodeType = tokens[3];
        row.setLocationBefore(tokens[4]);
        row.localNameBefore = tokens[5];
        row.setLocationAfter(tokens[6]);
        row.localNameAfter = tokens[7];
        row.setRefactoring(tokens[2]);
        row.refType = toRefType(tokens[2] + "_" + row.nodeType);
        return row;
    }

    Ref.RefType toRefType(String name) {
        String typeName = name.toUpperCase();
        var typeNameUnderscored = typeName.replaceAll(" ", "_");
        var refType = Ref.RefType.fromStringMap.get(typeNameUnderscored);
        return refType;
    }

    boolean isMatch(RdRow rd, RmRow rm) {
        boolean equalBeforeLocation = equalLocation(rd.getLocationBefore(), rm.getLocationBefore());
        boolean equalBeforeName = rd.localNameBefore.equals(rm.localNameBefore);
        boolean equalAfterLocation = equalLocation(rd.getLocationAfter(), rm.getLocationAfter());
        boolean equalAfterName = rd.localNameAfter.equals(rm.localNameAfter);
        return rd.commit.equals(rm.commit)
                && rm.refType.equals(rd.refType)
                && equalBeforeLocation
                && equalBeforeName
                && equalAfterLocation
                && equalAfterName;
    }

    private boolean equalLocation(SourceLocation rdLocation, SourceLocation rmLocation) {
        return rdLocation.start == rmLocation.start && rdLocation.end == rmLocation.end
                && rdLocation.getFilePath().equals(rmLocation.getFilePath());
    }
}
