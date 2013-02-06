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

public class ArtepoCopy extends Notifier {
    private Repo destinationRepo;
    private List<SourcePattern> patterns;
    private String sourcePromotionName;
    transient private SourceRepoStrategy sourceRepoStrategy;

    @DataBoundConstructor
    public ArtepoCopy(Repo destinationRepo, List<SourcePattern> patterns, String sourcePromotionName) {
        this.destinationRepo = destinationRepo;
        this.patterns = patterns;
        this.sourcePromotionName = sourcePromotionName;

        readResolve();
    }

    private Object readResolve() {
        this.sourceRepoStrategy = new DefaultSourceRepoStrategy();
        return this;
    }

    public Repo getDestinationRepo() {
        return destinationRepo;
    }

    public void setDestinationRepo(Repo destinationRepo) {
        this.destinationRepo = destinationRepo;
    }

    public List<SourcePattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<SourcePattern> patterns) {
        this.patterns = patterns;
    }

    public SourceRepoStrategy getSourceRepoStrategy() {
        return sourceRepoStrategy;
    }

    public void setSourceRepoStrategy(SourceRepoStrategy sourceRepoStrategy) {
        this.sourceRepoStrategy = sourceRepoStrategy;
    }

    public String getSourcePromotionName() {
        return sourcePromotionName;
    }

    public void setSourcePromotionName(String sourcePromotionName) {
        this.sourcePromotionName = sourcePromotionName;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        if (!isCopyAllowed(build, launcher, listener)) {
            listener.getLogger().println("Artepo backup cannot be run due to unsuccessful build");
        } else {
            Repo sourceRepo = getSourceRepoStrategy().getSourceRepo(this, build, launcher, listener);
            Repo destinationRepo = getDestinationRepo();

            listener.getLogger().println("Copy "+patterns+" from "+sourceRepo+" to "+destinationRepo);

            FilePath tempPath = createTempPath(build.getProject().getRootProject());
            String buildTag = getResolvedBuildTag(build, listener);
            RepoInfoProvider infoProvider = new BuildRepoInfoProvider(build, tempPath, listener);

            FilePath sourcePath = sourceRepo.prepareSource(infoProvider, buildTag);
            destinationRepo.copyFrom(infoProvider, sourcePath, patterns, buildTag);
        }

        return true;
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

    private boolean isCopyAllowed(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        return build.getResult().isBetterOrEqualTo(Result.UNSTABLE);
    }

    private String getResolvedBuildTag(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
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


    private static class BuildRepoInfoProvider implements RepoInfoProvider {
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
