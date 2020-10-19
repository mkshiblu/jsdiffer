package io.jsrminer.refactorings;

public enum RefactoringType {

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
