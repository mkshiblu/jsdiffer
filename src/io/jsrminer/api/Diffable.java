package io.jsrminer.api;

import io.jsrminer.uml.diff.Diff;

public interface Diffable<T extends Diffable, E extends Diff> {
    E diff(T diffable);
}