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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class ArtepoCopy extends Notifier {
    private Repo destinationRepo;
    private List<SourcePattern> patterns;
    transient private SourceRepoStrategy sourceRepoStrategy;

    @DataBoundConstructor
    public ArtepoCopy(Repo destinationRepo, List<SourcePattern> patterns) {
        this.destinationRepo = destinationRepo;
        this.patterns = patterns;

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

            FilePath tempPath = getTempPath(build);
            String buildTag = getResolvedBuildTag(build, listener);
            RepoInfoProvider infoProvider = new BuildRepoInfoProvider(build, tempPath, listener);

            FilePath sourcePath = sourceRepo.prepareSource(infoProvider, buildTag);
            destinationRepo.copyFrom(infoProvider, sourcePath, patterns, buildTag);
        }

        return true;
    }

    private FilePath getTempPath(AbstractBuild<?, ?> build) {
        AbstractProject project = build.getProject().getRootProject();
        FilePath baseTempPath = new FilePath(new File(System.getProperty("java.io.tmpdir")));
        FilePath tempPath = baseTempPath.child(ArtepoUtil.PLUGIN_NAME+"/"+project.getName());
        return tempPath;
    }

    private boolean isCopyAllowed(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        if (build.getResult().isWorseThan(Result.SUCCESS)) {
            return false;
        } else {
            return true;
        }
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
