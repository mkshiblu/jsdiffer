package io.jsrminer.uml.diff;

import io.jsrminer.uml.FunctionUtil;

import java.util.Comparator;

public class FileRenameComparator implements Comparator<SourceFileRenameDiff> {

    @Override
    public int compare(SourceFileRenameDiff o1, SourceFileRenameDiff o2) {
        double nameDistance1 = o1.getNormalizedNameDistance();
        double nameDistance2 = o2.getNormalizedNameDistance();
        if (nameDistance1 != nameDistance2) {
            return Double.compare(nameDistance1, nameDistance2);
        } else {
            double directoryPathDistance1 = o1.getNormalizedDiretoryPathDistance();
            double directoryPathDistance2 = o2.getNormalizedDiretoryPathDistance();
            return Double.compare(directoryPathDistance1, directoryPathDistance2);
        }
    }
}
