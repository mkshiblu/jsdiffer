package io.jsrminer.evaluation;

import io.jsrminer.api.IRefactoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Evaluator {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    final String evaluationFile = "resources\\evaluation\\rd_eval.txt";
    private final String project;
    private final String commit;
    EvaluationDataSet rd1 = new EvaluationDataSet();

    public Evaluator(String project, String commit) {
        this.project = project;
        this.commit = commit;
    }

    public void evaluate(List<IRefactoring> jsrMinerRefactorings) {
        log.info("Loading evaluation data...");
        loadData();
    //    diff(jsrMinerRefactorings, rd1);
    }

    private void diff(List<IRefactoring> jsrMinerRefactorings, EvaluationDataSet rd1) {
        var rd1Data = rd1.getRefsInCommit(this.commit);
        boolean matchFound;

        for (var rdRef : rd1Data) {
            matchFound = false;

            for (var refactoring : jsrMinerRefactorings) {
                if (isMatch(rdRef, refactoring)) {
                    matchFound = true;
                    break;
                }
            }

            if (!matchFound) {

            }
        }
    }

    private void loadData() {
        try (Stream<String> stream = Files.lines(Paths.get(evaluationFile))) {
            stream.skip(1).forEach(this::processRow);

        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    private void processRow(String line) {
        var row = new RdRow();

        var tokens = line.split("\t");

        row.repository = tokens[0];
        row.commit = tokens[1];
        row.refactoringName = tokens[2];
        row.nodeType = tokens[3];
        row.locationBefore = tokens[4];
        row.localNameBefore = tokens[5];
        row.locationAfter = tokens[6];
        row.localNameAfter = tokens[7];

        rd1.reportRow(row);
    }

    public static void main(String[] args) {
        new Evaluator(null, null).evaluate(null);
    }

    boolean isMatch(RdRow rd, IRefactoring refactoring) {
        return rd.commit == this.commit
                && refactoring.getName() == rd.getRmRefactoringName();
    }
}
