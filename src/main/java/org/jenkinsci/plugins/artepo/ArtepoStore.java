package org.jenkinsci.plugins.artepo;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArtepoStore extends Notifier {
    private String svnUrl;
    private String svnUser;
    private String svnPassword;
    private List<StoreItem> itemsToStore;

    @DataBoundConstructor
    public ArtepoStore(String svnUrl, String svnUser, String svnPassword, List<StoreItem> itemsToStore) {
        this.svnUrl = svnUrl;
        this.svnUser = svnUser;
        this.svnPassword = svnPassword;
        this.itemsToStore = itemsToStore !=null? itemsToStore :new ArrayList<StoreItem>();
    }

    public String getSvnUrl() {
        return svnUrl;
    }

    public String getSvnUser() {
        return svnUser;
    }

    public String getSvnPassword() {
        return svnPassword;
    }

    public List<StoreItem> getItemsToStore() {
        return itemsToStore;
    }



    @Override
    public ArtepoStoreDescriptor getDescriptor() {
        return (ArtepoStoreDescriptor)super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("svnUrl: "+svnUrl);
        listener.getLogger().println("svnUser: "+svnUser);
        listener.getLogger().println("svnPassword: "+svnPassword);
        listener.getLogger().println("items: "+itemsToStore);

        return true;
    }
}
