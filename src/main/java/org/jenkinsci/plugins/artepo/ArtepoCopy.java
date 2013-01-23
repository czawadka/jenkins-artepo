package org.jenkinsci.plugins.artepo;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import org.jenkinsci.plugins.artepo.repo.Repo;
import org.jenkinsci.plugins.artepo.repo.RepoInfoProvider;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class ArtepoCopy extends Notifier {
    private Repo sourceRepo;
    private Repo destinationRepo;
    private List<SourcePattern> patterns;
    private String buildTag;

    @DataBoundConstructor
    public ArtepoCopy(Repo sourceRepo, Repo destinationRepo, List<SourcePattern> patterns) {
        this.sourceRepo = sourceRepo;
        this.destinationRepo = destinationRepo;
        this.patterns = patterns;
        this.buildTag = null;
    }

    public Repo getSourceRepo() {
        return sourceRepo;
    }

    public void setSourceRepo(Repo sourceRepo) {
        this.sourceRepo = sourceRepo;
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

    public String getBuildTag() {
        return buildTag;
    }

    public void setBuildTag(String buildTag) {
        this.buildTag = buildTag;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        if (checkCanRun(build, launcher, listener)) {
            listener.getLogger().println("Copy from "+sourceRepo+" to "+destinationRepo);

            FilePath baseTempPath = new FilePath(build.getProject().getRootDir());
            final FilePath tempPath = baseTempPath.child("."+ArtepoUtil.PLUGIN_NAME);
            String buildTag = getResolvedBuildTag(build, listener);
            RepoInfoProvider infoProvider = new RepoInfoProvider() {
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
            };
            FilePath sourcePath = sourceRepo.prepareSource(infoProvider, buildTag);
            destinationRepo.copyFrom(infoProvider, sourcePath, patterns, buildTag);
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
        String buildTag = this.buildTag;
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
