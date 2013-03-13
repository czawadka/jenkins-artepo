package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ArtepoUtilTest {
    protected FileUtil util = new FileUtil();

    @After
    public void tearDown() throws IOException {
        util.close();
    }

    @Test
    public void testSyncCannotRemoveFolderWithSvnAndFile() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "a.txt", ".svn/bla", "dist/.svn/foo", "dist/b.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "a.txt", "dist/b.txt");

        ArtepoUtil.sync(dst, src, null);

        assertThat(util.listDirPaths(dst, false), containsInAnyOrder("a.txt", ".svn/", ".svn/bla",
                "dist/", "dist/.svn/", "dist/.svn/foo", "dist/b.txt"));
    }

    @Test
    public void testSyncMustRemoveFolderWithOnlySvn() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "a.txt", ".svn/bla", "dist/.svn/foo", "dist/b.txt" );
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "a.txt");

        ArtepoUtil.sync(dst, src, null);

        assertThat(util.listDirPaths(dst, false), containsInAnyOrder(".svn/", ".svn/bla", "a.txt"));
    }

    @Test
    public void testListOrphansListNonExistingFiles() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "a.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src);

        Collection<FilePath> orphans = ArtepoUtil.listOrphans(dst, src, null).values();

        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder("a.txt"));
    }

    @Test
    public void testListOrphansNotListExistingFiles() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "a.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "a.txt");

        Collection<FilePath> orphans = ArtepoUtil.listOrphans(dst, src, null).values();

        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder());
    }

    @Test
    public void testListOrphansListNonExistingDirs() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "dist/b.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "a.txt");

        Collection<FilePath> orphans = ArtepoUtil.listOrphans(dst, src, null).values();

        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder("dist/"));
    }

    @Test
    public void testListOrphansNotListExistingDirs() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "dist/b.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "dist/b.txt");

        Collection<FilePath> orphans = ArtepoUtil.listOrphans(dst, src, null).values();

        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder());
    }

    @Test
    public void testListOrphansListDeepOrphans() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "dist/webapp/c.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "dist/webapp/d.txt");

        Collection<FilePath> orphans = ArtepoUtil.listOrphans(dst, src, null).values();

        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder("dist/webapp/c.txt"));
    }

}
