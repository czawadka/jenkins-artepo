package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.containsInAnyOrder;
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
}
