package jenkins.plugins.slack;

import hudson.model.Run;

public class DisabledNotifier implements FineGrainedNotifier {
    public void started(Run<?, ?> r) {
    }

    public void deleted(Run<?, ?> r) {
    }

    public void finalized(Run<?, ?> r) {
    }

    public void completed(Run<?, ?> r) {
    }
}
