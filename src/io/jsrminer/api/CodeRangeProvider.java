package io.jsrminer.api;

import java.util.List;

public interface CodeRangeProvider {
    List<CodeRange> leftSide();
    List<CodeRange> rightSide();
}
