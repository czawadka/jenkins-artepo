package org.jenkinsci.plugins.artepo;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.remoting.Callable;
import hudson.remoting.RemoteOutputStream;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.util.IOException2;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;

import java.io.IOException;
import java.io.OutputStream;
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

    protected Repo findSourceRepo(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) {
        return getSourceRepoStrategy().getSourceRepo(this, build, launcher, listener);
    }

    protected int getResolvedBuildNumber(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
        int buildTag = build.getRootBuild().getNumber();
        return buildTag;
    }

    protected boolean isBuildSuccessful(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        return build.getResult().isBetterOrEqualTo(Result.SUCCESS);
    }

    static protected class BuildRepoInfoProvider implements RepoInfoProvider, Serializable {
        String projectName;
        FilePath workspace;
        OutputStream remoteStream;
        transient PrintStream logger;

        public BuildRepoInfoProvider(String projectName, FilePath workspace, OutputStream logger) {
            this.projectName = projectName;
            this.workspace = workspace;
            this.remoteStream = new RemoteOutputStream(logger);
        }

        public boolean isBuildActive() {
            return true;
        }

        public FilePath getTempPath() throws IOException, InterruptedException {
            return createNodeTempPath(null, projectName);
        }

        public PrintStream getLogger() {
            if (logger==null) {
                try {
                    logger = new PrintStream(remoteStream, true, "UTF-8");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return logger;
        }

        public FilePath getWorkspacePath() {
            return workspace;
        }
    }

    static protected class CopyCallable implements Callable<Object, IOException> {
        Repo destinationRepo;
        Repo sourceRepo;
        CopyPattern copyPattern;
        RepoInfoProvider infoProvider;
        int buildNumber;

        public CopyCallable(Repo destinationRepo, Repo sourceRepo, CopyPattern copyPattern,
                            RepoInfoProvider infoProvider, int buildNumber) {
            this.destinationRepo = destinationRepo;
            this.sourceRepo = sourceRepo;
            this.copyPattern = copyPattern;
            this.infoProvider = infoProvider;
            this.buildNumber = buildNumber;
        }

        public Object call() throws IOException {
            try {
                FilePath sourcePath = sourceRepo.prepareSource(infoProvider, buildNumber);
                destinationRepo.copyFrom(infoProvider, sourcePath, copyPattern, buildNumber);
                return null;
            } catch (InterruptedException e) {
                throw new IOException2(e);
            }
        }
    }

    protected void copy(AbstractBuild build, BuildListener listener,
                            final Repo destinationRepo, final Repo sourceRepo,
                            final CopyPattern copyPattern)
            throws InterruptedException, IOException {

        RepoInfoProvider infoProvider = new BuildRepoInfoProvider(
                build.getProject().getRootProject().getName(),
                build.getWorkspace(),
                listener.getLogger()
            );
        int buildNumber = getResolvedBuildNumber(build, listener);

        Callable<Object, IOException> callable = new CopyCallable(
                destinationRepo, sourceRepo, copyPattern, infoProvider, buildNumber
            );
        Node node = build.getBuiltOn();
        node.getChannel().call(callable);
    }
}
