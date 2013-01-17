package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ArtepoUtilTest {
    FilePath baseDir;
    FilePath src;
    FilePath dst;

    @Before
    public void setUp() throws IOException, InterruptedException {
        baseDir = new FilePath(new File(System.getProperty("java.io.tmpdir"))).createTempDir("artepo-test","");
        src = baseDir.child("src");
        dst = baseDir.child("dst");
        src.mkdirs();
        dst.mkdirs();

        src.child("a.txt").touch(System.currentTimeMillis());
        src.child("b.txt").touch(System.currentTimeMillis());
        dst.child("c.txt").touch(System.currentTimeMillis());
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        baseDir.deleteRecursive();
    }

    @Test
    public void syncDeletesUnnecessary() throws IOException, InterruptedException {
        ArtepoUtil.sync(dst, src, null);

        List<FilePath> files = dst.list();
        assertThat(files, not(contains(dst.child("c.txt"))));
    }

    @Test
    public void syncCopy() throws IOException, InterruptedException {
        ArtepoUtil.sync(dst, src, null);

        List<FilePath> files = dst.list();
        assertThat(files, contains(dst.child("a.txt"), dst.child("b.txt")));
    }

    @Test
    public void syncCopyWithManyIncludes() throws IOException, InterruptedException {
        List<BackupSource> sources = new ArrayList<BackupSource>();
        sources.add(new BackupSource(null, "a.*", null));
        sources.add(new BackupSource(null, "b.*", null));
        ArtepoUtil.sync(dst, src, sources);

        List<FilePath> files = dst.list();
        assertThat(files, contains(dst.child("a.txt"), dst.child("b.txt")));
    }

    @Test
    public void syncDeleteWithManyIncludes() throws IOException, InterruptedException {
        List<BackupSource> sources = new ArrayList<BackupSource>();
        sources.add(new BackupSource(null, "a.*", null));
        sources.add(new BackupSource(null, "b.*", null));
        ArtepoUtil.sync(dst, src, sources);

        List<FilePath> files = dst.list();
        assertThat(files, not(contains(dst.child("c.txt"))));
    }
}
