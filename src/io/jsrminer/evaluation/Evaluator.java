package io.jsrminer.evaluation;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.RefactoringType;
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
    private final String project;
    private final String commit;
    EvaluationDataSet<RdRow> rdDataSet = new EvaluationDataSet<>();
    EvaluationDataSet<RmRow> rmDataSet = new EvaluationDataSet<>();

    public Evaluator(String project, String commit) {
        this.project = project;
        this.commit = commit;
    }

    public static void main(String[] args) {
        String repoPath = "E:\\PROJECTS_REPO\\vue";
        String commitId = "a08feed8c410b89fa049fdbd6b9459e2d858e912";

        new Evaluator("vue", commitId).evaluate();
    }

    public void evaluate() {
        loadDatasets();
        diff();
    }

    public void loadDatasets() {
        loadRdData(this.rdEvaluationFile);
        loadRmData(this.rmEvaluationFile);
    }

    public void diff() {
        var rdRefs = rdDataSet.getRefsInCommit(commit);
        var rmRefs = rmDataSet.getRefsInCommit(commit);
        diffCommitRefs(rdRefs, rmRefs);
    }

    private ComparisonResult diffCommitRefs(List<RdRow> rdRefs, List<RmRow> rmRefs) {

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

        ComparisonResult result = diffRefTypeMaps(rdRefTypeMap, rmRefTypeMap);

        return result;
    }

    private ComparisonResult diffRefTypeMaps(Map<Ref.RefType, List<RdRow>> rdRefTypeMap, Map<Ref.RefType, List<RmRow>> rmRefTypeMap) {
        ComparisonResult result = new ComparisonResult();

        for (var rdEntry : rdRefTypeMap.entrySet()) {
            var refType = rdEntry.getKey();
            var rdRefactorings = rdRefTypeMap.get(refType);
            var rmRefactorings = rmRefTypeMap.get(refType);

            if (rmRefactorings != null) {
                matchSameTypeRefactorings(rdRefactorings, rmRefactorings, result);
            } else {
                result.totalMissedInRdTypes.put(refType, rdRefactorings);
            }
        }

        return result;
    }

    private void matchSameTypeRefactorings(List<RdRow> rdRefactorings, List<RmRow> rmRefactorings, ComparisonResult result) {
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
                result.matchedCount++;
            } else {
                result.unmatchedCount++;
                result.unmatchedRds.computeIfAbsent(rdRef.refType, key -> new ArrayList<>()).add(rdRef);
            }
        }
    }

    private void loadRdData(String filePath) {
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.skip(1).forEach(line -> {
                var row = processRdRow(line);
                rdDataSet.reportRow(row);
            });

        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    private void loadRmData(String filePath) {
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.skip(1).forEach(line -> {
                var row = parseRmRefactoring(line);
                rmDataSet.reportRow(row);
            });

        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    private RmRow parseRmRefactoring(String line) {
        var row = new RmRow();
        var tokens = line.split("\t");
        row.repository = tokens[0];
        row.commit = tokens[1];
        row.refactoring = tokens[4];
        row.refType = toRefType(tokens[2].replaceAll("(?i)method", "function"));
        return row;
    }

    private RdRow processRdRow(String line) {
        var row = new RdRow();

        var tokens = line.split("\t");

        row.repository = tokens[0];
        row.commit = tokens[1];
        row.nodeType = tokens[3];
        row.locationBefore = tokens[4];
        row.localNameBefore = tokens[5];
        row.locationAfter = tokens[6];
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
        switch (rd.refType){
            case MOVE_FILE:
                //if(rd.localNameBefore.contains(rm.) )
                break;
        }

        return rd.commit.equals(this.commit)
                && rm.refType.equals(rd.refType)
                ;
    }

    class ComparisonResult {
        Map<Ref.RefType, List<RdRow>> totalMissedInRdTypes = new HashMap<>();
        Map<Ref.RefType, List<RdRow>> totalMissedInRmTypes = new HashMap<>();

        Map<Ref.RefType, List<RdRow>> unmatchedRds = new HashMap<>();
        Map<Ref.RefType, List<RmRow>> unmatchedRms = new HashMap<>();
        int unmatchedCount = 0;
        int matchedCount = 0;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            if (totalMissedInRdTypes.size() > 0) {
                builder.append("Missed Refactorings Type in RD: ");
                builder.append(totalMissedInRdTypes.keySet().toString());
                builder.append(System.lineSeparator());
            }
            builder.append("Unmatched Count: " + unmatchedCount + "\n");
            builder.append("Matched Count: " + matchedCount + "\n");

            return builder.toString();
        }
    }
}
