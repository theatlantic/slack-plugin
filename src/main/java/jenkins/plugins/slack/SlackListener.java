package jenkins.plugins.slack;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;

import java.util.Map;
import java.util.logging.Logger;

@Extension
public class SlackListener extends RunListener<Run<?, ?>> {

    private static final Logger logger = Logger.getLogger(SlackListener.class.getName());

    public SlackListener() {
        super((Class<Run<?, ?>>)(Class<?>)Run.class);
    }

    @Override
    public void onCompleted(Run<?, ?> r, TaskListener listener) {
        getNotifier(r.getParent(), listener).completed(r);
        super.onCompleted(r, listener);
    }

    @Override
    public void onStarted(Run<?, ?> r, TaskListener listener) {
        // getNotifier(r.getParent()).started(r);
        // super.onStarted(r, listener);
    }

    @Override
    public void onDeleted(Run<?, ?> r) {
        // getNotifier(r.getParent()).deleted(r);
        // super.onDeleted(r);
    }

    @Override
    public void onFinalized(Run<?, ?> r) {
         getNotifier(r.getParent(), null).finalized(r);
         super.onFinalized(r);
    }

    @SuppressWarnings("unchecked")
    FineGrainedNotifier getNotifier(Job<?, ?> parent, TaskListener listener) {
        if (parent instanceof AbstractProject) {
            AbstractProject<?, ?> project = (AbstractProject<?, ?>)parent;
            Map<Descriptor<Publisher>, Publisher> map = project.getPublishersList().toMap();
            for (Publisher publisher : map.values()) {
                if (publisher instanceof SlackNotifier) {
                    return new ActiveNotifier((SlackNotifier) publisher, listener);
                }
            }
        }
        return new DisabledNotifier();
    }

}
