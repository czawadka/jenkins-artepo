package org.jenkinsci.plugins.artepo;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

public class ArtepoBackup extends Notifier {
    private Repo repo;
    private String buildTag;
    private List<BackupSource> sources;

    @DataBoundConstructor
    public ArtepoBackup(Repo repo, String buildTag, List<BackupSource> sources) {
        this.repo = repo;
        this.buildTag = buildTag;
        this.sources = sources;
    }

    public Repo getRepo() {
        return repo;
    }

    void setRepo(Repo repo) {
        this.repo = repo;
    }

    public String getBuildTag() {
        return buildTag;
    }

    public void setBuildTag(String buildTag) {
        this.buildTag = buildTag;
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
        if (checkCanRun(build, launcher, listener)) {
            String buildTag = getResolvedBuildTag(build, listener);
            listener.getLogger().println("Backup with '"+buildTag+"' tag");
            repo.backup(build, launcher, listener, buildTag, sources );
        }

        return true;
    }

    private boolean checkCanRun(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        if (build.getResult().isWorseThan(Result.SUCCESS)) {
            listener.getLogger().println("Artepo backup cannot be run due to unsuccessful build");
            return false;
        } else {
            return true;
        }
    }

    private String getResolvedBuildTag(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
        EnvVars env = build.getEnvironment(listener);
        String  buildTag = this.buildTag;
        if (buildTag!=null && buildTag.trim().length()>0) {
            buildTag = env.expand(buildTag.trim());
        } else {
            buildTag = env.get(ArtepoUtil.PROMOTED_NUMBER);
            if (buildTag==null || buildTag.trim().length()==0) {
                buildTag = String.valueOf(build.getNumber());
            }
        }
        return buildTag;
    }

}
