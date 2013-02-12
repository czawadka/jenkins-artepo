package org.jenkinsci.plugins.artepo;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class ArtepoBase extends Notifier {
    private String sourcePromotionName;
    transient private SourceRepoStrategy sourceRepoStrategy;

    public ArtepoBase(String sourcePromotionName) {
        this.sourcePromotionName = sourcePromotionName;

        readResolve();
    }

    protected Object readResolve() {
        this.sourceRepoStrategy = new DefaultSourceRepoStrategy();
        return this;
    }

    public String getSourcePromotionName() {
        return sourcePromotionName;
    }

    public void setSourcePromotionName(String sourcePromotionName) {
        this.sourcePromotionName = sourcePromotionName;
    }

    public SourceRepoStrategy getSourceRepoStrategy() {
        return sourceRepoStrategy;
    }

    public void setSourceRepoStrategy(SourceRepoStrategy sourceRepoStrategy) {
        this.sourceRepoStrategy = sourceRepoStrategy;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    static public FilePath getTempPath(AbstractProject<?, ?> project, Node node) throws IOException, InterruptedException {
        FilePath baseTempPath = ArtepoUtil.getRemoteTempPath(node);
        if (baseTempPath==null)
            return null; // node is offline
        return baseTempPath.child(ArtepoUtil.PLUGIN_NAME+"/"+project.getRootProject().getName());
    }

    static public FilePath createTempPath(AbstractProject<?, ?> project) throws IOException, InterruptedException {
        FilePath tempPath = getTempPath(project, null);
        if (tempPath.exists())
            tempPath.touch(System.currentTimeMillis());
        else
            tempPath.mkdirs();
        return tempPath;
    }

    protected RepoInfoProvider createRepoInfoProvider(AbstractBuild<?, ?> build,
                                                           BuildListener listener) throws IOException, InterruptedException {
        FilePath tempPath = createTempPath(build.getProject().getRootProject());
        return new BuildRepoInfoProvider(build, tempPath, listener);
    }

    protected Repo findSourceRepo(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) {
        return getSourceRepoStrategy().getSourceRepo(this, build, launcher, listener);
    }

    protected String getResolvedBuildTag(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
        EnvVars env = build.getEnvironment(listener);
        String buildTag = null;
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

    protected static class BuildRepoInfoProvider implements RepoInfoProvider {
        private final AbstractBuild<?, ?> build;
        private final FilePath tempPath;
        private final BuildListener listener;

        public BuildRepoInfoProvider(AbstractBuild<?, ?> build, FilePath tempPath, BuildListener listener) {
            this.build = build;
            this.tempPath = tempPath;
            this.listener = listener;
        }

        public boolean isBuildActive() {
            return build.isBuilding();
        }

        public FilePath getTempPath() {
            return tempPath;
        }

        public PrintStream getLogger() {
            return listener.getLogger();
        }

        public FilePath getWorkspacePath() {
            return build.getWorkspace();
        }
    }
}
