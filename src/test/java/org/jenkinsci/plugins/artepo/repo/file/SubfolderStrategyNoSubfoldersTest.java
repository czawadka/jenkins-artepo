package org.jenkinsci.plugins.artepo.repo.file;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class SubfolderStrategyNoSubfoldersTest extends AbstractSubfolderStrategyTest {

    public SubfolderStrategyNoSubfoldersTest() {
        super(SubfolderStrategy.NoSubfolders);
    }

    @Test
    public void testGetDestinationPathsMustReturnRepoPath() throws Exception {
        Assert.assertThat(destinationPaths, Matchers.containsInAnyOrder(repoPath));
    }

    @Test
    public void testGetPotentialSourcePathsMustReturnRepoPath() throws Exception {
        Assert.assertThat(potentialSourcePaths, Matchers.containsInAnyOrder(repoPath));
    }
}
