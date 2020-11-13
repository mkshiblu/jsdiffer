package io.jsrminer.refactorings;

public enum RefactoringType {

    EXTRACT_OPERATION("Extract Method", "Extract Method (.+) extracted from (.+) in class (.+)"/*, 2*/),
    EXTRACT_AND_MOVE_OPERATION("Extract And Move Method", "Extract And Move Method (.+) extracted from (.+) in class (.+) & moved to class (.+)"),
    INLINE_OPERATION("Inline Method", "Inline Method (.+) inlined to (.+) in class (.+)"),
    MOVE_AND_INLINE_OPERATION("Move And Inline Method", "Move And Inline Method (.+) moved from class (.+) to class (.+) & inlined to (.+)"),
    ADD_PARAMETER("Add Parameter", "Add Parameter (.+) in method (.+) from class (.+)"),
    REMOVE_PARAMETER("Remove Parameter", "Remove Parameter (.+) in method (.+) from class (.+)");

    private String title;
    private String description;

    RefactoringType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public String toString() {
        return title + " " + description;
    }
}
