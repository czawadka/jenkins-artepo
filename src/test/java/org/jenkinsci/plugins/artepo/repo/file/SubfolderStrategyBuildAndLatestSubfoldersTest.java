package org.jenkinsci.plugins.artepo.repo.file;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class SubfolderStrategyBuildAndLatestSubfoldersTest extends AbstractSubfolderStrategyTest {

    public SubfolderStrategyBuildAndLatestSubfoldersTest() {
        super(SubfolderStrategy.BuildAndLatestSubfolders);
    }

    @Test
    public void testGetDestinationPathsMustReturnFormattedBuildAndLatestFolders() throws Exception {
        Assert.assertThat(destinationPaths, Matchers.containsInAnyOrder(
                SubfolderStrategy.getFormattedBuildSubfolder(repoPath, buildNumber),
                repoPath.child("latest")));
    }

    @Test
    public void testGetPotentialSourcePathsMustReturnOnlyFormattedBuildFolder() throws Exception {
        Assert.assertThat(potentialSourcePaths, Matchers.containsInAnyOrder(
                SubfolderStrategy.getFormattedBuildSubfolder(repoPath, buildNumber)));
    }
}
