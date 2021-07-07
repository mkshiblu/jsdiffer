package io.jsrminer.uml.diff;

import io.jsrminer.uml.ClassUtil;

import java.util.Comparator;

public class ClassRenameComparator implements Comparator<UMLClassRenameDiff> {

    @Override
    public int compare(UMLClassRenameDiff o1, UMLClassRenameDiff o2) {
        double nameDistance1 = o1.normalizedNameDistance();
        double nameDistance2 = o2.normalizedNameDistance();

        if (nameDistance1 != nameDistance2) {
            return Double.compare(nameDistance1, nameDistance2);
        } else {
            double packageDistance1 = o1.normalizedContainerQualifiedNameDistance();
            double packageDistance2 = o2.normalizedContainerQualifiedNameDistance();
            return Double.compare(packageDistance1, packageDistance2);
        }
    }
}
