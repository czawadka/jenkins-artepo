package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

public class AbstractTest {
    protected FilePath tempBaseDir;

    private FilePath createTempDir() {
        try {
            return new FilePath(new File(System.getProperty("java.io.tmpdir"))).createTempDir("artepo-test","");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void createTempBaseDir() throws IOException, InterruptedException {
        tempBaseDir = createTempDir();
    }

    public FilePath getTempBaseDir() {
        return tempBaseDir;
    }

    protected FilePath createTempSubDir(String subFolder) throws IOException, InterruptedException {
        FilePath subDir = subFolder==null ? tempBaseDir.createTempDir("artepo", "") : tempBaseDir.child(subFolder);
        subDir.mkdirs();
        return subDir;
    }

    @After
    public void deleteTempBaseDir() throws IOException, InterruptedException {
        if (tempBaseDir!=null) {
            tempBaseDir.deleteRecursive();
            tempBaseDir = null;
        }
    }

}
