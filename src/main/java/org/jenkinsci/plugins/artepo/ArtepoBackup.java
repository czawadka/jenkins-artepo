package org.jenkinsci.plugins.artepo;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

public class ArtepoBackup extends Notifier {
    private Repo repo;
    private List<BackupSource> sources;

    @DataBoundConstructor
    public ArtepoBackup(Repo repo, List<BackupSource> sources) {
        this.repo = repo;
        this.sources = sources;
    }

    public Repo getRepo() {
        return repo;
    }

    void setRepo(Repo repo) {
        this.repo = repo;
    }

    public List<BackupSource> getSources() {
        return sources;
    }

    public void setSources(List<BackupSource> sources) {
        this.sources = sources;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("repo: "+ repo);
        listener.getLogger().println("sources: "+ sources);

        String buildTag = getResolvedBuildTag(build);
        repo.backup(build, launcher, listener, buildTag, sources );

        return true;
    }

    private String getResolvedBuildTag(AbstractBuild<?, ?> build) {
        return String.valueOf(build.getNumber());
    }

}
