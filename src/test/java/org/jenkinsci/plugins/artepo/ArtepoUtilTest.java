package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
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
    public void testSyncToSvnWithManySvnSubFolders() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "a.txt", ".svn/bla", "dist/.svn/foo", "dist/b.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "c/d.txt");

        ArtepoUtil.sync(dst, src, null);

        assertThat(util.listDirPaths(dst, false), containsInAnyOrder(".svn/", ".svn/bla", "c/", "c/d.txt"));
    }

    @Test
    public void testListOrphansListNonExistingFiles() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "a.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src);

        List<FilePath> orphans = ArtepoUtil.listOrphans(dst, src);

        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder("a.txt"));
    }

    @Test
    public void testListOrphansNotListExistingFiles() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "a.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "a.txt");

        List<FilePath> orphans = ArtepoUtil.listOrphans(dst, src);

        if (orphans==null)
            orphans = Collections.EMPTY_LIST;
        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder());
    }

    @Test
    public void testListOrphansReturnNullIfEmpty() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "a.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "a.txt");

        List<FilePath> orphans = ArtepoUtil.listOrphans(dst, src);

        assertNull(orphans);
    }

    @Test
    public void testListOrphansListNonExistingDirs() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "dist/b.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "a.txt");

        List<FilePath> orphans = ArtepoUtil.listOrphans(dst, src);

        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder("dist/"));
    }

    @Test
    public void testListOrphansNotListExistingDirs() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "dist/b.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "dist/b.txt");

        List<FilePath> orphans = ArtepoUtil.listOrphans(dst, src);

        if (orphans==null)
            orphans = Collections.EMPTY_LIST;
        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder());
    }

    @Test
    public void testListOrphansListDeepOrphans() throws IOException, InterruptedException {
        FilePath dst = util.createTempSubDir("dst");
        util.replaceFiles(dst, "dist/webapp/c.txt");
        FilePath src = util.createTempSubDir("src");
        util.replaceFiles(src, "dist/webapp/d.txt");

        List<FilePath> orphans = ArtepoUtil.listOrphans(dst, src);

        if (orphans==null)
            orphans = Collections.EMPTY_LIST;
        assertThat(util.filesToPaths(dst, orphans), containsInAnyOrder("dist/webapp/c.txt"));
    }

}
