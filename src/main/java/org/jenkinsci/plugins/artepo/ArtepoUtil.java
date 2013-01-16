package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sync;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class ArtepoUtil {
    static public final String PROMOTED_NUMBER = "PROMOTED_NUMBER";
    static public final String PLUGIN_NAME = "artepo";

    static public File toFile(FilePath filePath) {
        return new File(filePath.getRemote());
    }

    static public void sync(final FilePath dst, final FilePath src, final Collection<BackupSource> items) throws IOException, InterruptedException {
        try {
            Sync syncTask = new Sync();
            syncTask.setTodir(toFile(dst));
            syncTask.setOverwrite(true);
            syncTask.setIncludeEmptyDirs(false);

            for(BackupSource item : items) {
                FilePath itemSrc = item.getDir()==null||item.getDir().length()==0 ? src : src.child(item.getDir());
                syncTask.addFileset(hudson.Util.createFileSet(toFile(itemSrc), item.getIncludes(), item.getExcludes()));
            }

            syncTask.execute();

        } catch (BuildException e) {
            throw new IOException("Failed to sync " + src + "/" + items + " to " + dst, e);
        }
    }

    static public void deleteRecursive(File file) throws IOException, InterruptedException {
        new FilePath(file).deleteRecursive();
    }
}
