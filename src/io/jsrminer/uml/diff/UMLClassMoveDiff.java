package io.jsrminer.uml.diff;

import com.google.javascript.jscomp.deps.PathUtil;
import io.jsrminer.io.FileUtil;
import io.rminerx.core.api.IClassDeclaration;

import java.nio.file.Path;

public class UMLClassMoveDiff {

    IClassDeclaration originalClass;
    IClassDeclaration nextClass;

    UMLClassMoveDiff(IClassDeclaration originalClass, IClassDeclaration movedClass) {
        this.originalClass = originalClass;
        this.nextClass = movedClass;
    }

    public IClassDeclaration getNextClass() {
        return this.nextClass;
    }

    public IClassDeclaration getOriginalClass() {
        return originalClass;
    }

    public double normalizedSourceFolderDistance() {
        String s1 = FileUtil.getFolder(originalClass.getSourceLocation().getFilePath().toLowerCase());
        String s2 = FileUtil.getFolder(nextClass.getSourceLocation().getFilePath().toLowerCase());
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }
}
