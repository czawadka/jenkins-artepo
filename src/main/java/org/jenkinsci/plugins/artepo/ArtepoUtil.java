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

public class ArtepoUtil {
    static public final String PROMOTED_NUMBER = "PROMOTED_NUMBER";
    static public final String PLUGIN_NAME = "artepo";

    static public File toFile(FilePath filePath) {
        return new File(filePath.getRemote());
    }

    static private final List<String> SYNC_DST_PRESERVE_PATHS = Arrays.asList(".git", ".svn" );

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

    static public List<FilePath> deleteOrphans(FilePath dst, FilePath src, Collection<String> preservePaths) throws IOException, InterruptedException {
        final List<FilePath> dstFilesToDelete = listOrphans(dst, src);

        if (dstFilesToDelete!=null) {
            if (preservePaths!=null) {
                for (String preservePath : preservePaths) {
                    dstFilesToDelete.remove(dst.child(preservePath));
                }
            }

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

    static public List<FilePath> listOrphans(FilePath dst, FilePath src) throws IOException, InterruptedException {
        List<FilePath> orphans = null;

        List<FilePath> dstChildren = dst.list();
        if (dstChildren!=null) {
            Collection<FilePath> srcChildren = src.list();
            if (srcChildren==null) { // src has no children means all dst children are orphans
                orphans = dstChildren;
            } else {
                srcChildren = new LinkedHashSet<FilePath>(srcChildren);
                for(FilePath dstChild : dstChildren) {
                    FilePath srcChild = src.child(dstChild.getName());
                    if (!srcChildren.contains(srcChild)) {
                        if (orphans==null)
                            orphans = new ArrayList<FilePath>();
                        orphans.add(dstChild);
                    } else {
                        List<FilePath> childOrphans = listOrphans(dstChild, srcChild);
                        if (childOrphans!=null) {
                            if (orphans==null)
                                orphans = new ArrayList<FilePath>(childOrphans.size());
                            orphans.addAll(childOrphans);
                        }
                    }
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
