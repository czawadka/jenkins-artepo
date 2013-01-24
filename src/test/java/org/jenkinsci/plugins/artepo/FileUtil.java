package org.jenkinsci.plugins.artepo;

import hudson.FilePath;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FileUtil implements Closeable {
    protected FilePath tempBaseDir;

    public FilePath createTempDir() {
        try {
            return new FilePath(new File(System.getProperty("java.io.tmpdir"))).createTempDir("artepo-test","");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public FilePath getTempBaseDir() {
        if (tempBaseDir==null) {
            tempBaseDir = createTempDir();
        }
        return tempBaseDir;
    }

    public FilePath createTempSubDir(String subFolder) throws IOException, InterruptedException {
        FilePath tempBaseDir = getTempBaseDir();
        FilePath subDir = subFolder==null ? tempBaseDir.createTempDir("artepo", "") : tempBaseDir.child(subFolder);
        subDir.mkdirs();
        return subDir;
    }

    public void close() throws IOException {
        if (tempBaseDir!=null) {
            try {
                tempBaseDir.deleteRecursive();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                tempBaseDir = null;
            }
        }
    }

    public List<FilePath> listDirFiles(FilePath source) throws IOException, InterruptedException {
        return Arrays.asList(source.list("**/*", null, true));
    }

    public List<String> listDirPaths(FilePath source) throws IOException, InterruptedException {
        Collection<FilePath> files = listDirFiles(source);
        return filesToPaths(source, files);
    }

    public List<String> filesToPaths(FilePath baseFile, Collection<FilePath> files) throws IOException, InterruptedException {
        ArrayList<String> paths = new ArrayList<String>(files.size());
        String sourceName = baseFile.getRemote();
        for (FilePath file : files) {
            String path = file.getRemote();
            path = path.substring(sourceName.length()+1);
            path = path.replace('\\', '/');
            if (file.isDirectory())
                path += "/";
            paths.add(path);
        }
        return paths;
    }

}
