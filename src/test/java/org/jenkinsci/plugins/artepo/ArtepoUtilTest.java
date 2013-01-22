package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ArtepoUtilTest extends AbstractTest {
    FilePath src;
    FilePath dst;

    @Before
    public void setUp() throws IOException, InterruptedException {
        src = tempBaseDir.child("src");
        dst = tempBaseDir.child("dst");
        src.mkdirs();
        dst.mkdirs();

        src.child("a.txt").touch(System.currentTimeMillis());
        src.child("b.txt").touch(System.currentTimeMillis());
        dst.child("c.txt").touch(System.currentTimeMillis());
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

    @Test
    public void syncDontDeleteScmDirInDst() throws IOException, InterruptedException {
        FilePath dstGit = dst.child(".git");
        dstGit.mkdirs();

        ArtepoUtil.sync(dst, src, null);

        assertTrue(dstGit+" doesn't exist", dstGit.exists());
    }


}
