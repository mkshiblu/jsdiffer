package io.jsrminer.evaluation;

import com.google.javascript.jscomp.jarjar.com.google.common.base.Strings;
import io.jsrminer.evaluation.util.RefactoringFormatterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Evaluator {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    final String rdEvaluationFile = "resources\\evaluation\\rd_prestaging.txt";
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
        System.out.println(diff);
        saveValidations(diff);
      //  stats(diff);
    }

    void stats(DatasetDiff diff) {
        int tps = truePositiveCount(diff, "angular.js");
    }

    int truePositiveCount(DatasetDiff diff, String project) {
        return diff.getProjectCommonRefactorings(project).size();
    }

    int falsePositiveCount(DatasetDiff diff, String project) {
        return diff.getProjectCommonRefactorings(project).size();
    }

    void saveValidations(DatasetDiff diff) {
        var util = new RefactoringFormatterUtil();
        var csvTable = util.formatAsCsv(rmDataSet.getRefactorings());
        try {
            FileWriter myWriter = new FileWriter("rm_automated_tps.txt");
            myWriter.write(csvTable);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
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

                if (isRefactoringSupportedByOtherTools(row.refType))
                    rmDataSet.addRefactoring(row);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    boolean isRefactoringSupportedByOtherTools(Ref.RefType refType) {
        switch (refType) {
            case EXTRACT_AND_MOVE_FUNCTION:
            case MOVE_AND_RENAME_FILE:
            case MOVE_AND_RENAME_FUNCTION:
            case RENAME_FUNCTION:
            case MOVE_FUNCTION:
            case MOVE_FILE:
            case RENAME_FILE:
            case EXTRACT_FUNCTION:
            case INLINE_FUNCTION:
            case CONVERT_TYPE_FUNCTION:
            case CONVERT_TYPE_CLASS:
            case MOVE_CLASS:
            case RENAME_CLASS:
                return true;
            default:
                return false;
        }
    }

    private RmRow parseRmRefactoring(String line, int lineNo) {
        var row = new RmRow();
        row.lineNo = lineNo;
        var tokens = line.split("\t");
        row.project = tokens[0];
        row.commitId = tokens[1];
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
        var tokens = line.split("\t", -1);

        row.id = tokens[0];
        row.project = formatToCommonProjectName(tokens[1]);
        row.commitId = tokens[2];
        row.refType = toRefType(tokens[3]);
        //  row.nodeType = tokens[3];
        row.localNameBefore = tokens[4];
        row.localNameAfter = tokens[5];
        row.setLocationBefore(tokens[6]);
        row.setLocationAfter(tokens[7]);
      //  row.setRefactoring(tokens[2]);

        return row;
    }

    Ref.RefType toRefType(String name) {
        String typeName = name.toUpperCase();
        var typeNameUnderscored = typeName.replaceAll(" ", "_");
        var refType = Ref.RefType.fromStringMap.get(typeNameUnderscored);

        if(refType == null){
            throw  new RuntimeException("Ref Type not foound " + name);
        }
        return refType;
    }

    private String formatToCommonProjectName(String project) {
        return project.replace(".git", "");
    }
}
