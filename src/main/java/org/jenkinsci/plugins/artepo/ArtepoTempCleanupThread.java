package org.jenkinsci.plugins.artepo;

import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.AsyncPeriodicWork;
import hudson.model.Node;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Clean up on all nodes unused artpo temporary directories
 */
@Extension
public class ArtepoTempCleanupThread extends AsyncPeriodicWork {
    private static final Logger LOGGER = Logger.getLogger(ArtepoTempCleanupThread.class.getName());
    /**
     * Can be used to disable workspace clean up.
     */
    static public final boolean disabled = Boolean.getBoolean(ArtepoTempCleanupThread.class.getName()+".disabled");
    /**
     * Number of days after which temp repository will be removed
     */
    static public final int DAYS_TO_CLEAN_AFTER = 14;

    public ArtepoTempCleanupThread() {
        super("artepo temp clean-up");
    }

    private List<AbstractProject> projects;
    private TaskListener listener;

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        try {
            if(disabled) {
                LOGGER.fine("Disabled. Skipping execution");
                return;
            }

            Jenkins jenkins = Jenkins.getInstance();
            this.projects = Util.createSubList(jenkins.getItemMap().values(), AbstractProject.class);
            this.listener = listener;

            for (Node node : jenkins.getNodes()) {
                cleanNode(node);
            }
            cleanNode(jenkins);
        } finally {
            this.projects = null;
            this.listener = null;
        }
    }

    private void cleanNode(Node node) throws IOException, InterruptedException {
        listener.getLogger().println("Scanning "+node.getNodeName());
        try {
            for (AbstractProject project : projects) {
                FilePath projectTempPath = ArtepoCopy.getTempPath(project, node);
                if (shouldDeleteDir(projectTempPath))
                    delete(projectTempPath);
            }
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed on "+node.getNodeName()));
        }
    }

    private boolean shouldDeleteDir(FilePath dir) throws IOException, InterruptedException {
        if (dir==null) {
            return false;
        } else if (!dir.exists()) {
            return false;
        } else {
            long now = System.currentTimeMillis();
            long tstamp = dir.lastModified();
            if (tstamp + DAYS_TO_CLEAN_AFTER * DAY > now) {
                LOGGER.fine("Directory "+dir+" is only "+ Util.getTimeSpanString(now-tstamp)+" old, so not deleting");
                return false;
            }
            LOGGER.finer("Going to delete directory "+dir);
            return true;
        }
    }

    private void delete(FilePath dir) throws InterruptedException {
        try {
            listener.getLogger().println("Deleting "+dir);
            dir.deleteRecursive();
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to delete "+dir));
        }
    }

    @Override
    public long getRecurrencePeriod() {
        return DAY;
    }
}
