package org.jenkinsci.plugins.artepo.repo;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.artepo.ArtepoUtil;
import org.jenkinsci.plugins.artepo.BackupSource;

import java.io.IOException;
import java.util.List;

abstract public class ScmRepo extends Repo {

    @Override
    protected boolean backup(FilePath srcPath, String buildTag, List<BackupSource> backupSources)
            throws InterruptedException, IOException {

        FilePath tempPath = getScmWorkPath(build);
        listener.getLogger();
        listener.getLogger().println("Preparing working folder in " + tempPath);
        tempPath.mkdirs();

        listener.getLogger().println("Checkout to working folder");
        checkout(tempPath, null);

        listener.getLogger().println("Sync working folder with src");
        ArtepoUtil.sync(tempPath, srcPath, backupSources);

        listener.getLogger().println("Commit working folder");
        commit(tempPath, buildTag);

        return true;
    }

    protected FilePath getScmWorkPath(AbstractBuild<?, ?> build) {
        return build.getWorkspace().child(".artepo/"+getType());
    }

    abstract public boolean checkout(FilePath dstPath, String buildTag) throws InterruptedException, IOException;
    abstract public boolean commit(FilePath srcPath, String buildTag) throws InterruptedException, IOException;
}
