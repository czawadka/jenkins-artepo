package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import hudson.Util;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

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

    public List<FilePath> listDirFiles(FilePath dir) throws IOException, InterruptedException {
        return listDirFiles(dir, true);
    }
    public List<FilePath> listDirFiles(FilePath dir, boolean defaultExcludes) throws IOException, InterruptedException {
        return listDirFiles(dir, null, null, defaultExcludes);
    }

    public List<String> listDirPaths(FilePath dir) throws IOException, InterruptedException {
        return listDirPaths(dir, true);
    }
    public List<String> listDirPaths(FilePath dir, boolean defaultExcludes) throws IOException, InterruptedException {
        return listDirPaths(dir, null, null, defaultExcludes);
    }
    private static List<FilePath> listDirFiles(FilePath dir, String includes, String excludes, boolean defaultExcludes) throws IOException {
        List<String> paths = listDirPaths(dir, includes, excludes, defaultExcludes);
        List<FilePath> files = new ArrayList<FilePath>(paths.size());
        for (String path : paths) {
            files.add(dir.child(path));
        }
        return files;
    }
    private static List<String> listDirPaths(FilePath dir, String includes, String excludes, boolean defaultExcludes) throws IOException {
        if (includes==null)
            includes = "**/*";
        FileSet fs = Util.createFileSet(ArtepoUtil.toFile(dir), includes, excludes);
        fs.setDefaultexcludes(defaultExcludes);
        DirectoryScanner ds = fs.getDirectoryScanner(new Project());
        String[] files = ds.getIncludedFiles();
        String[] directories = ds.getIncludedDirectories();
        List<String> paths = new ArrayList<String>(files.length+directories.length);
        for (String file : files) {
            paths.add(file.replace('\\', '/'));
        }
        for (String directory : directories) {
            paths.add(directory.replace('\\', '/')+"/");
        }
        return paths;
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

    public FilePath replaceFiles(FilePath dir, String... newPaths) throws IOException, InterruptedException {
        List<FilePath> paths = dir.list();
        for (FilePath path : paths) {
            if (path.isDirectory())
                path.deleteRecursive();
            else
                path.delete();
        }

        for(String fileName : newPaths) {
            dir.child(fileName).write(fileName, "UTF-8");
        }

        return dir;
    }

}
