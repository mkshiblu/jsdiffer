package io.jsrminer.evaluation;

import com.google.javascript.jscomp.jarjar.com.google.common.base.Strings;
import io.jsrminer.sourcetree.SourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Evaluator {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    final String rdEvaluationFile = "resources\\evaluation\\rd_eval.txt";
    final String rmEvaluationFile = "resources\\evaluation\\rm_eval_data.txt";
    Dataset rdDataSet = new Dataset();
    Dataset rmDataSet = new Dataset();

    public Evaluator() {
    }

    public static void main(String[] args) {
        new Evaluator().evaluateAll();
    }

    public void evaluateAll() {
        loadDatasets();
        var differ = new DatasetsDiffer(rdDataSet, rmDataSet);
        var diff = differ.diff();

        RefactoringValidator refactoringValidator = new RefactoringValidator(rmDataSet.getRefactorings());
        var truePositives = refactoringValidator.validateTruePositives(rdDataSet.getRefactorings());

        System.out.println(diff);
        System.out.println("True Positives: ");
        truePositives.forEach(ref -> System.out.println(ref));
    }

    void validateTruePositives() {

    }

    void saveValidations() {

    }

    public void evaluate(String project, String commit) {
        loadDatasets();
        var differ = new DatasetsDiffer(rdDataSet, rmDataSet);
        differ.diffRepositoryCommit(project, commit);
    }

    public void loadDatasets() {
        loadRdData(this.rdEvaluationFile);
        loadRmData(this.rmEvaluationFile);
    }

    private void loadRdData(String filePath) {
        try {
            var lines = Files.readAllLines(Paths.get(filePath));
            for (int i = 1; i < lines.size(); i++) {
                var row = processRdRow(lines.get(i), i + 1);
                rdDataSet.addRefactoring(row);
            }

        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    private void loadRmData(String filePath) {
        try {
            var lines = Files.readAllLines(Paths.get(filePath));
            for (int i = 1; i < lines.size(); i++) {
                var row = parseRmRefactoring(lines.get(i), i + 1);
                rmDataSet.addRefactoring(row);
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
        row.refType = toRefType(tokens[2].replaceAll("(?i)(method|operation)", "function"));

        if (tokens.length == 8) {
            row.localNameBefore = tokens[3];
            row.localNameAfter = tokens[4];

            if (!Strings.isNullOrEmpty(tokens[5]))
                row.setLocationBefore(tokens[5]);

            if (!Strings.isNullOrEmpty(tokens[6]))
                row.setLocationAfter(tokens[6]);

        }
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
}
