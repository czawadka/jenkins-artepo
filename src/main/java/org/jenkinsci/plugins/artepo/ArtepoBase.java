package org.jenkinsci.plugins.artepo;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.util.IOException2;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

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

    static public FilePath getNodeTempPath(Node node, String projectName) throws IOException, InterruptedException {
        FilePath baseTempPath = ArtepoUtil.getRemoteTempPath(node);
        if (baseTempPath==null)
            return null; // node is offline
        return baseTempPath.child(ArtepoUtil.PLUGIN_NAME+"/"+projectName);
    }

    static public FilePath createNodeTempPath(Node node, String projectName) throws IOException, InterruptedException {
        FilePath tempPath = getNodeTempPath(node, projectName);
        if (tempPath.exists())
            tempPath.touch(System.currentTimeMillis());
        else
            tempPath.mkdirs();
        return tempPath;
    }

    protected RepoInfoProvider createRepoInfoProvider(String projectName,
                                                           FilePath workspace) throws IOException, InterruptedException {
        return new BuildRepoInfoProvider(projectName, workspace);
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

    protected boolean isBuildSuccessful(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        return build.getResult().isBetterOrEqualTo(Result.SUCCESS);
    }

    protected static class BuildRepoInfoProvider implements RepoInfoProvider, Serializable {
        String projectName;
        FilePath workspace;

        public BuildRepoInfoProvider(String projectName, FilePath workspace) {
            this.projectName = projectName;
            this.workspace = workspace;
        }

        public boolean isBuildActive() {
            return true;
        }

        public FilePath getTempPath() throws IOException, InterruptedException {
            return createNodeTempPath(null, projectName);
        }

        public PrintStream getLogger() {
            return System.out;
        }

        public FilePath getWorkspacePath() {
            return workspace;
        }
    }

    static public void copy(Node node, final Repo destinationRepo, final Repo sourceRepo, final CopyPattern copyPattern,
                            final RepoInfoProvider infoProvider, final String buildTag)
            throws InterruptedException, IOException {
        Callable<Object, IOException> callable = new Callable<Object, IOException>() {
            public Object call() throws IOException {
                try {
                    FilePath sourcePath = sourceRepo.prepareSource(infoProvider, buildTag);
                    destinationRepo.copyFrom(infoProvider, sourcePath, copyPattern, buildTag);
                    return null;
                } catch (InterruptedException e) {
                    throw new IOException2(e);
                }
            }
        };
        node.getChannel().call(callable);
    }
}
