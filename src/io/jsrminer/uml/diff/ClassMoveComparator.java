package io.jsrminer.uml.diff;

import java.util.Comparator;

public class ClassMoveComparator implements Comparator<UMLClassMoveDiff> {
    @Override
    public int compare(UMLClassMoveDiff o1, UMLClassMoveDiff o2) {
        double sourceFolderDistance1 = o1.normalizedSourceFolderDistance();
        double sourceFolderDistance2 = o2.normalizedSourceFolderDistance();
        return Double.compare(sourceFolderDistance1, sourceFolderDistance2);
    }
}
