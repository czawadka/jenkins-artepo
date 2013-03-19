package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AbstractSubfolderStrategyTest {
    protected SubfolderStrategy subfolderStrategy;

    List<FilePath> destinationPaths;
    List<FilePath> potentialSourcePaths;
    FilePath repoPath;
    int buildNumber;

    protected AbstractSubfolderStrategyTest(SubfolderStrategy subfolderStrategy) {
        this.subfolderStrategy = subfolderStrategy;
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        repoPath = new FilePath(new File("."));
        buildNumber = 13;
        destinationPaths = subfolderStrategy.getDestinationPaths(repoPath, buildNumber);
        potentialSourcePaths = subfolderStrategy.getPotentialSourcePaths(repoPath, buildNumber);
    }
}
