package jenkins.plugins.slack;

import hudson.model.Run;

public interface FineGrainedNotifier {

    void started(Run<?, ?> r);

    void deleted(Run<?, ?> r);

    void finalized(Run<?, ?> r);

    void completed(Run<?, ?> r);

}
