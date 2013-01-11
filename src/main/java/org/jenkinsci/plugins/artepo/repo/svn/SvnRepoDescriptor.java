package org.jenkinsci.plugins.artepo.repo.svn;

import hudson.Extension;
import org.jenkinsci.plugins.artepo.repo.RepoDescriptor;

@Extension
public class SvnRepoDescriptor extends RepoDescriptor {

    public SvnRepoDescriptor() {
        super(SvnRepo.class, "svn");
    }
    public String getDisplayName() {
        return "Svn";
    }
}
