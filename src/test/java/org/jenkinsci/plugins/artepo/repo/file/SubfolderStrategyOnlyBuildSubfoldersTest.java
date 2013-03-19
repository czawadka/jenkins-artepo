package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SubfolderStrategyOnlyBuildSubfoldersTest extends AbstractSubfolderStrategyTest {

    public SubfolderStrategyOnlyBuildSubfoldersTest() {
        super(SubfolderStrategy.OnlyBuildSubfolders);
    }

    @Test
    public void testGetDestinationPathsMustReturnOnlyFormattedBuildFolder() throws Exception {
        Assert.assertThat(destinationPaths, Matchers.containsInAnyOrder(SubfolderStrategy.getFormattedBuildSubfolder(repoPath, buildNumber)));
    }

    @Test
    public void testGetPotentialSourcePathsMustReturnFormattedBuildFolder() throws Exception {
        Assert.assertThat(potentialSourcePaths, Matchers.hasItem(SubfolderStrategy.getFormattedBuildSubfolder(repoPath, buildNumber)));
    }

    @Test
    public void testGetPotentialSourcePathsMustReturnUnFormattedBuildFolder() throws Exception {
        Assert.assertThat(potentialSourcePaths, Matchers.hasItem(SubfolderStrategy.getUnFormattedBuildSubfolder(repoPath, buildNumber)));
    }
}
