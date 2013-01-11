package org.jenkinsci.plugins.artepo.repo.file;

import hudson.FilePath;
import org.jenkinsci.plugins.artepo.BackupSource;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileRepo extends Repo {
    private String dstPath;

    @DataBoundConstructor
    public FileRepo(String dstPath) {
        this.dstPath = dstPath;
    }

    public FileRepo() {
        this(null);
    }

    public String getDstPath() {
        return dstPath;
    }

    public void setDstPath(String dstPath) {
        this.dstPath = dstPath;
    }

    @Override
    protected boolean backup(FilePath srcPath, String buildTag, List<BackupSource> backupSources)
            throws InterruptedException, IOException {

        FilePath buildDstPath = new FilePath(new File(dstPath)).child(buildTag);
        buildDstPath.mkdirs();

        for(BackupSource backupSource : backupSources) {
            listener.getLogger().println("Backup "+backupSource+" to "+buildDstPath);
            FilePath sourceSrcPath = backupSource.getDir()!=null ? srcPath.child(backupSource.getDir()) : srcPath;
            sourceSrcPath.copyRecursiveTo(backupSource.getIncludes(), backupSource.getExcludes(), buildDstPath);
        }
        return true;
    }
}
