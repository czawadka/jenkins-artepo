package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.FileUtil;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FileRepoCleanupThreadTest {
    FileUtil util = new FileUtil();
    FileRepoCleanupThread cleanupThread = new FileRepoCleanupThread();

    @After
    public void tearDown() throws Exception {
        util.close();
    }

    @Test
    public void testFindBuildDirsToDeleteNaturalOrdering() throws Exception {
        FilePath basePath = util.createTempDir();
        for (int i = 8; i < 8+FileRepoCleanupThread.buildsToKeep+1; i++) {
            basePath.child(String.valueOf(i)).mkdirs();
        }
        FileRepo repo = new FileRepo(basePath.getRemote());

        List<FilePath> dirsToDelete = cleanupThread.findBuildDirsToDelete(repo);

        assertThat(util.filesToPaths(basePath, dirsToDelete), containsInAnyOrder("8/"));
    }

    @Test
    public void testFindBuildDirsToDeleteIsEmptyIfBuildsAreLessThenToKeep() throws Exception {
        FilePath basePath = util.createTempDir();
        for (int i = 0; i < FileRepoCleanupThread.buildsToKeep-2; i++) {
            basePath.child(String.valueOf(i)).mkdirs();
        }
        FileRepo repo = new FileRepo(basePath.getRemote());

        List<FilePath> dirsToDelete = cleanupThread.findBuildDirsToDelete(repo);
        assertEquals(0, dirsToDelete.size());
    }

    @Test
    public void testFindBuildDirsToDeleteShouldReturnOnlyBuildFolders() throws Exception {
        FilePath basePath = util.createTempDir();
        for (int i = 1; i < 1+FileRepoCleanupThread.buildsToKeep+1; i++) {
            basePath.child(String.valueOf(i)).mkdirs();
        }
        basePath.child("latest").mkdirs();
        FileRepo repo = new FileRepo(basePath.getRemote());

        List<FilePath> dirsToDelete = cleanupThread.findBuildDirsToDelete(repo);
        assertThat(util.filesToPaths(basePath, dirsToDelete), containsInAnyOrder("1/"));
    }
}
