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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Sync;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class ArtepoUtil {
    static public final String PROMOTED_NUMBER = "PROMOTED_NUMBER";
    static public final String PLUGIN_NAME = "artepo";

    static public File toFile(FilePath filePath) {
        return new File(filePath.getRemote());
    }

    static private final Pattern SYNC_DST_PRESERVE_PATHS = Pattern.compile("^(\\.git/|\\.svn/|.*/\\.svn/)$");

    static public void sync(FilePath dst, FilePath src, CopyPattern pattern) throws IOException, InterruptedException {
        String includes = null;
        String excludes = null;
        if (pattern!=null) {
            if (pattern.getSubFolder()!=null && pattern.getSubFolder().length()>0)
                src = src.child(pattern.getSubFolder());
            includes = pattern.getIncludes();
            excludes = pattern.getExcludes();
        }
        if (includes==null)
            includes = "";

        sync(dst, src, includes, excludes);
    }

    static public void sync(FilePath dst, FilePath src, String includes, String excludes) throws IOException, InterruptedException {
        deleteOrphans(dst, src, SYNC_DST_PRESERVE_PATHS);
        src.copyRecursiveTo(includes, excludes, dst);
    }

    static public Collection<FilePath> deleteOrphans(FilePath dst, FilePath src, Pattern preservePathsPattern) throws IOException, InterruptedException {
        final Collection<FilePath> dstFilesToDelete = listOrphans(dst, src, preservePathsPattern).values();

        if (dstFilesToDelete!=null) {

            dst.act(new FilePath.FileCallable<Object>() {
                public Object invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                    for (FilePath fileToDelete : dstFilesToDelete) {
                        fileToDelete.deleteRecursive();
                    }
                    return null;
                }
            });
        }

        return dstFilesToDelete;
    }

    static public Map<String, FilePath> listOrphans(FilePath dst, FilePath src,
                                             Pattern preserveFilePattern)
            throws IOException, InterruptedException {

        Map<String,FilePath> orphans = new LinkedHashMap<String, FilePath>();
        LinkedList<String> pathsToCheck = new LinkedList<String>();
        pathsToCheck.add("");

        while(!pathsToCheck.isEmpty()) {
            String currentPath = pathsToCheck.removeFirst();

            List<FilePath> dstChildren = dst.child(currentPath).list();
            if (dstChildren==null) {
                continue;
            }

            Collection<FilePath> srcChildren = src.child(currentPath).list();
            if (srcChildren==null) {
                srcChildren = Collections.EMPTY_LIST;
            } else {
                srcChildren = new HashSet<FilePath>(srcChildren);
            }

            for(FilePath dstChild : dstChildren) {
                String path = currentPath+dstChild.getName() + (dstChild.isDirectory() ? "/": "");
                if (preserveFilePattern!=null && preserveFilePattern.matcher(path).matches()) {
                    continue;
                }
                FilePath srcChild = src.child(path);
                if (!srcChildren.contains(srcChild)) {
                    orphans.put(path, dstChild);
                } else if (path.endsWith("/")) {
                    pathsToCheck.add(path);
                }
            }
        }

        return orphans;
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
