package org.jsrminer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.jsrminer.api.GitService;
import org.jsrminer.util.JGitService;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        log.debug("Program Starts");
        GitService git = new JGitService();
        try {
            Repository repo = git.cloneIfNotExists(
                    "tmp/refactoring-toy-example",
                    "https://github.com/danilofes/refactoring-toy-example.git");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("-----------------Program Ends--------------------");
    }
}
