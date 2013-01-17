package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

public class TestBase extends ArtepoUtil {
    protected FilePath baseDir;

    public FilePath createTempDir() throws IOException, InterruptedException {
        return new FilePath(new File(System.getProperty("java.io.tmpdir"))).createTempDir("artepo-test","");
    }

    @Before
    public void createBaseDir() throws IOException, InterruptedException {
        baseDir = createTempDir();
    }

    @After
    public void deleteBaseDir() throws IOException, InterruptedException {
        if (baseDir!=null) {
            baseDir.deleteRecursive();
            baseDir = null;
        }
    }

}
