package org.jenkinsci.plugins.artepo;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Sync;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ArtepoUtil {
    static public final String PROMOTED_NUMBER = "PROMOTED_NUMBER";
    static public final String PLUGIN_NAME = "artepo";

    static public File toFile(FilePath filePath) {
        return new File(filePath.getRemote());
    }

    static public void sync(FilePath dst, FilePath src, Collection<CopyPattern> patterns) throws IOException, InterruptedException {
        try {
            Sync syncTask = new Sync();
            syncTask.setProject(new Project());
            syncTask.init();
            syncTask.setTodir(toFile(dst));
            syncTask.setOverwrite(true);
            syncTask.setIncludeEmptyDirs(false);

            if (patterns==null || patterns.isEmpty()) {
                patterns = new ArrayList<CopyPattern>(1);
                patterns.add(new CopyPattern(null, null, null));
            }

            for(CopyPattern item : patterns) {
                FilePath itemSrc =
                        item.getSubFolder()==null || item.getSubFolder().length()==0
                        ? src
                        : src.child(item.getSubFolder());
                String includes = item.getIncludes();
                FileSet fs = hudson.Util.createFileSet(toFile(itemSrc),
                        includes == null ? "" : includes,
                        item.getExcludes());
                fs.setDefaultexcludes(true);
                syncTask.addFileset(fs);
            }

            Sync.SyncTarget preserveInDst = new Sync.SyncTarget();
            String[] defaultExcludes = DirectoryScanner.getDefaultExcludes();
            for (String defaultExclude : defaultExcludes) {
                preserveInDst.createInclude().setName(defaultExclude);
            }
            preserveInDst.setDefaultexcludes(false);
            syncTask.addPreserveInTarget(preserveInDst);
            syncTask.setIncludeEmptyDirs(true);

            syncTask.execute();

        } catch (BuildException e) {
            throw new IOException("Failed to sync " + src + "/" + patterns + " to " + dst, e);
        }
    }

    static public void deleteRecursive(File file) throws IOException, InterruptedException {
        new FilePath(file).deleteRecursive();
    }

    static public FilePath getRemoteTempPath(VirtualChannel channel) throws IOException, InterruptedException {
        String temp;
        if (channel==null)
            temp = getSystemTempPathCallable.call();
        else
            temp = channel.call(getSystemTempPathCallable);
        return new FilePath(channel, temp);
    }
    static public FilePath getRemoteTempPath(Node node) throws IOException, InterruptedException {
        if (node==null)
            return getRemoteTempPath((VirtualChannel)null);
        VirtualChannel channel = node.getChannel();
        if (channel==null)
            return null; // node is offline
        return getRemoteTempPath(channel);
    }

    static protected Callable<String, RuntimeException> getSystemTempPathCallable = new Callable<String, RuntimeException>() {
            public String call() {
                return System.getProperty("java.io.tmpdir");
            }
        };

    static public ArtepoCopy findMainArtepo(AbstractProject project) {
        AbstractProject rootProject = project.getRootProject();
        DescribableList<Publisher,Descriptor<Publisher>> publishers = rootProject.getPublishersList();
        Descriptor<Publisher> artepoCopyDescriptor = Jenkins.getInstance().getDescriptor(ArtepoCopy.class);

        ArtepoCopy artepoCopy = null;
        for (Publisher p : publishers) {
            if(p.getDescriptor()==artepoCopyDescriptor) {
                artepoCopy = (ArtepoCopy)p;
                break;
            }
        }

        return  artepoCopy;
    }
}
