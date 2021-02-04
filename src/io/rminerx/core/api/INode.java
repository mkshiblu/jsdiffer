package io.rminerx.core.api;

import io.jsrminer.sourcetree.SourceLocation;

/**
 * Represents a source code Node
 */
public interface INode {
    SourceLocation getSourceLocation();
}
