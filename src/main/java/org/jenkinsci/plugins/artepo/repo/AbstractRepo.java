package org.jenkinsci.plugins.artepo.repo;

import hudson.DescriptorExtensionList;
import hudson.FilePath;
import hudson.model.Describable;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.util.IOException2;
import org.jenkinsci.plugins.artepo.CopyPattern;
import org.jenkinsci.plugins.artepo.repo.file.FileRepo;

import java.io.IOException;
import java.util.List;

abstract public class AbstractRepo implements Describable<AbstractRepo>, Repo {

    abstract protected AbstractRepoImpl createImpl(RepoInfoProvider infoProvider);

    public FilePath prepareSource(RepoInfoProvider infoProvider, int buildNumber) throws InterruptedException, IOException {
        AbstractRepoImpl impl = createImpl(infoProvider);
        return impl.prepareSource(buildNumber);
    }

    public void copyFrom(RepoInfoProvider infoProvider, FilePath sourcePath, CopyPattern pattern, int buildNumber) throws InterruptedException, IOException {
        AbstractRepoImpl impl = createImpl(infoProvider);
        impl.copyFrom(sourcePath, pattern, buildNumber);
    }

    public RepoDescriptor getDescriptor() {
        return (RepoDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    static public DescriptorExtensionList<AbstractRepo,RepoDescriptor> all() {
        return Hudson.getInstance().<AbstractRepo,RepoDescriptor>getDescriptorList(AbstractRepo.class);
    }

    static public RepoDescriptor getDescriptorByType(String type) {
        for(RepoDescriptor descriptor : all()) {
            if (descriptor.getType().equals(type))
                return descriptor;
        }
        return null;
    }

    static public AbstractRepo getDefaultRepo() {
        return new FileRepo();
    }
}
