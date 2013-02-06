package org.jenkinsci.plugins.artepo.repo.file;

import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.artepo.ArtepoCopy;
import org.jenkinsci.plugins.artepo.NaturalOrderComparator;
import org.jenkinsci.plugins.artepo.repo.Repo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Clean up copied build from main level artepo build step
 */
@Extension
public class FileRepoCleanupThread extends AsyncPeriodicWork {
    private static final Logger LOGGER = Logger.getLogger(FileRepoCleanupThread.class.getName());
    /**
     * Can be used to disable workspace clean up.
     */
    static public final boolean disabled = Boolean.getBoolean(FileRepoCleanupThread.class.getName() + ".disabled");
    /**
     * Number of newest builds that should be kept for main level artepo
     */
    static public final int buildsToKeep = Integer.getInteger(FileRepoCleanupThread.class.getName() + ".buildsToKeep", 3);
    /**
     * How often (in hours) clean should be run
     */
    static public final int frequency = Integer.getInteger(FileRepoCleanupThread.class.getName() + ".frequency", 2);

    public FileRepoCleanupThread() {
        super("artepo FileRepo clean-up");
    }

    private TaskListener listener;

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        try {
            if(disabled) {
                LOGGER.fine("Disabled. Skipping execution");
                return;
            }

            Jenkins jenkins = Jenkins.getInstance();
            List<AbstractProject> projects = Util.createSubList(jenkins.getItemMap().values(), AbstractProject.class);
            this.listener = listener;

            for (AbstractProject project : projects) {
                cleanProject(project);
            }

        } finally {
            this.listener = null;
        }
    }

    void cleanProject(AbstractProject project) throws InterruptedException {
        listener.getLogger().println("Scanning "+project.getName());
        try {
            ArtepoCopy artepo = (ArtepoCopy)project.getPublishersList().get(Jenkins.getInstance().getDescriptorOrDie(ArtepoCopy.class));
            if (artepo!=null) {
                Repo repo = artepo.getDestinationRepo();
                if (repo instanceof FileRepo) {
                    cleanFileRepo((FileRepo)repo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed on "+project.getName()));
        }
    }

    void cleanFileRepo(FileRepo fileRepo) throws IOException, InterruptedException {
        List<FilePath> dirsToDelete = findDirsToDelete(fileRepo);
        for (FilePath dir : dirsToDelete) {
            delete(dir);
        }
    }

    List<FilePath> findDirsToDelete(FileRepo fileRepo) throws IOException, InterruptedException {
        String path = fileRepo.getPath();
        FilePath dir = new FilePath(new File(path));
        List<FilePath> subDirs = dir.list();
        if (subDirs==null)
            return Collections.EMPTY_LIST;
        Collections.sort(subDirs, newestBuildsAreLastComparator);
        return subDirs.subList(0, subDirs.size()-buildsToKeep);
    }

    void delete(FilePath dir) throws InterruptedException {
        try {
            listener.getLogger().println("Deleting "+dir);
            dir.deleteRecursive();
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to delete "+dir));
        }
    }

    @Override
    public long getRecurrencePeriod() {
        return frequency * HOUR;
    }

    static protected Comparator newestBuildsAreLastComparator = new NaturalOrderComparator();
}
