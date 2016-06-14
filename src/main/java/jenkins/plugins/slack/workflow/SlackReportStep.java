package jenkins.plugins.slack.workflow;

import hudson.AbortException;
import hudson.Extension;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.ActiveNotifier;
import jenkins.plugins.slack.CommitInfoChoice;
import jenkins.plugins.slack.DisabledNotifier;
import jenkins.plugins.slack.FineGrainedNotifier;
import jenkins.plugins.slack.Messages;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.SlackService;
import jenkins.plugins.slack.StandardSlackService;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecution;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Workflow step to report the start and end of a run as a Slack channel notification.
 */

public class SlackReportStep extends AbstractStepImpl {

    private String token;
    private boolean botUser;
    private String channel;
    private String baseUrl;
    private String teamDomain;
    private boolean failOnError;

    private String authTokenCredentialId;
    private String sendAs;
    private boolean startNotification = true;
    private boolean notifySuccess = true;
    private boolean notifyAborted = true;
    private boolean notifyNotBuilt = true;
    private boolean notifyUnstable = true;
    private boolean notifyRegression = true;
    private boolean notifyFailure = true;
    private boolean notifyBackToNormal = true;
    private boolean notifyRepeatedFailure = true;
    private boolean includeTestSummary = true;
    private boolean includeFailedTests = true;
    private CommitInfoChoice commitInfoChoice = CommitInfoChoice.NONE;
    private boolean includeCustomMessage;
    private String customMessage;

    public String getToken() {
        return token;
    }

    @DataBoundSetter
    public void setToken(String token) {
        this.token = Util.fixEmpty(token);
    }

    public boolean getBotUser() {
        return botUser;
    }

    @DataBoundSetter
    public void setBotUser(boolean botUser) {
        this.botUser = botUser;
    }

    public String getChannel() {
        return channel;
    }

    @DataBoundSetter
    public void setChannel(String channel) {
        this.channel = Util.fixEmpty(channel);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @DataBoundSetter
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = Util.fixEmpty(baseUrl);
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    @DataBoundSetter
    public void setTeamDomain(String teamDomain) {
        this.teamDomain = Util.fixEmpty(teamDomain);
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    @DataBoundSetter
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @DataBoundSetter
    public void setAuthTokenCredentialId(String authTokenCredentialId) {
        this.authTokenCredentialId = Util.fixEmpty(authTokenCredentialId);
    }

    public String getAuthTokenCredentialId() {
        return authTokenCredentialId;
    }

    @DataBoundSetter
    public void setSendAs(String sendAs) {
        this.sendAs = Util.fixEmpty(sendAs);
    }

    public String getSendAs() {
        return sendAs;
    }

    @DataBoundSetter
    public void setStartNotification(boolean startNotification) {
        this.startNotification = startNotification;
    }

    public boolean getStartNotification() {
        return startNotification;
    }

    @DataBoundSetter
    public void setNotifySuccess(boolean notifySuccess) {
        this.notifySuccess = notifySuccess;
    }

    public boolean getNotifySuccess() {
        return notifySuccess;
    }

    @DataBoundSetter
    public void setNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
    }

    public boolean getNotifyAborted() {
        return notifyAborted;
    }

    @DataBoundSetter
    public void setNotifyNotBuilt(boolean notifyNotBuilt) {
        this.notifyNotBuilt = notifyNotBuilt;
    }

    public boolean getNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    @DataBoundSetter
    public void setNotifyUnstable(boolean notifyUnstable) {
        this.notifyUnstable = notifyUnstable;
    }

    public boolean getNotifyUnstable() {
        return notifyUnstable;
    }

    @DataBoundSetter
    public void setNotifyRegression(boolean notifyRegression) {
        this.notifyRegression = notifyRegression;
    }

    public boolean getNotifyRegression() {
        return notifyRegression;
    }

    @DataBoundSetter
    public void setNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
    }

    public boolean getNotifyFailure() {
        return notifyFailure;
    }

    @DataBoundSetter
    public void setNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
    }

    public boolean getNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    @DataBoundSetter
    public void setNotifyRepeatedFailure(boolean notifyRepeatedFailure) {
        this.notifyRepeatedFailure = notifyRepeatedFailure;
    }

    public boolean getNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    @DataBoundSetter
    public void setIncludeTestSummary(boolean includeTestSummary) {
        this.includeTestSummary = includeTestSummary;
    }

    public boolean getIncludeTestSummary() {
        return includeTestSummary;
    }

    @DataBoundSetter
    public void setIncludeFailedTests(boolean includeFailedTests) {
        this.includeFailedTests = includeFailedTests;
    }

    public boolean getIncludeFailedTests() {
        return includeFailedTests;
    }

    @DataBoundSetter
    public void setCommitInfoChoice(CommitInfoChoice commitInfoChoice) {
        this.commitInfoChoice = commitInfoChoice;
    }

    public CommitInfoChoice getCommitInfoChoice() {
        return commitInfoChoice;
    }

    @DataBoundSetter
    public void setIncludeCustomMessage(boolean includeCustomMessage) {
        this.includeCustomMessage = includeCustomMessage;
    }

    public boolean getIncludeCustomMessage() {
        return includeCustomMessage;
    }

    @DataBoundSetter
    public void setCustomMessage(String customMessage) {
        this.customMessage = Util.fixEmpty(customMessage);
    }

    public String getCustomMessage() {
        return customMessage;
    }

    @DataBoundConstructor
    public SlackReportStep() {
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public static final CommitInfoChoice[] COMMIT_INFO_CHOICES = CommitInfoChoice.values();

        public DescriptorImpl() {
            super(SlackReportStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "slackReport";
        }

        @Override
        public String getDisplayName() {
            return Messages.SlackReportStepDisplayName();
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }

        public Step newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            if (formData.has("includeCustomMessage")) {
                try {
                    JSONObject obj = formData.getJSONObject("includeCustomMessage");
                    if (obj.has("customMessage")) {
                        String customMessage = obj.getString("customMessage");
                        formData.element("customMessage", customMessage);
                        formData.element("includeCustomMessage", true);
                    } else {
                        formData.element("includeCustomMessage", false);
                    }
                } catch (JSONException e) {
                    // Probably a boolean or just plain incorrect; let it be handled normally
                }
            }
            return super.newInstance(req, formData);
        }

    }

    public static class SlackReportStepExecution extends AbstractStepExecutionImpl {

        private static final long serialVersionUID = 1L;

        @Inject
        transient SlackReportStep step;

        @StepContextParameter
        transient TaskListener listener;

        private volatile BodyExecution body;

        private FineGrainedNotifier getNotifier() {
            //default to global config values if not set in step, but allow step to override all global settings
            Jenkins jenkins;
            //Jenkins.getInstance() may return null, no message sent in that case
            try {
                jenkins = Jenkins.getInstance();
            } catch (NullPointerException ne) {
                listener.error(Messages.NotificationFailedWithException(ne));
                return new DisabledNotifier();
            }
            SlackNotifier.DescriptorImpl slackDesc = jenkins.getDescriptorByType(SlackNotifier.DescriptorImpl.class);
            String baseUrl = step.baseUrl != null ? step.baseUrl : slackDesc.getBaseUrl();
            String team = step.teamDomain != null ? step.teamDomain : slackDesc.getTeamDomain();
            String authTokenCredentialId = step.authTokenCredentialId != null ? step.authTokenCredentialId : slackDesc.getTokenCredentialId();
            String token;
            boolean botUser;
            if (step.token != null) {
                token = step.token;
                botUser = step.botUser;
            } else {
                token = slackDesc.getToken();
                botUser = slackDesc.getBotUser();
            }
            String channel = step.channel != null ? step.channel : slackDesc.getRoom();

            //placing in console log to simplify testing of retrieving values from global config or from step field; also used for tests
            listener.getLogger().println(Messages.SlackReportStepConfig(step.baseUrl == null, step.teamDomain == null, step.token == null, step.channel == null));

            SlackNotifier notifier = new SlackNotifier(baseUrl, team, token, botUser, channel, authTokenCredentialId, step.sendAs, step.startNotification, step.notifyAborted,
                step.notifyFailure, step.notifyNotBuilt, step.notifySuccess, step.notifyUnstable, step.notifyRegression, step.notifyBackToNormal, step.notifyRepeatedFailure,
                step.includeTestSummary, step.includeFailedTests, step.commitInfoChoice, step.includeCustomMessage, step.customMessage);
            return new ActiveNotifier(notifier, listener);
        }

        @Override
        public boolean start() throws Exception {
            body = getContext().newBodyInvoker().withCallback(new BodyExecutionCallback.TailCall() {
                public void onStart(StepContext context) {
                    if (step.startNotification) {
                        try {
                            getNotifier().started(context.get(Run.class));
                        } catch (Exception e) {
                            listener.error(Messages.NotificationFailedWithException(e));
                        }
                    }
                }

                public void finished(StepContext context) throws Exception {
                    getNotifier().completed(context.get(Run.class));
                }
            }).start();
            return false;   // execution is asynchronous
        }

        @Override
        public void stop(Throwable cause) throws Exception {
            if (body!=null)
                body.cancel(cause);
        }

        //streamline unit testing
        SlackService getSlackService(String baseUrl, String team, String authToken, String authTokenCredentialId, boolean botUser, String channel) {
            return new StandardSlackService(baseUrl, team, authToken, authTokenCredentialId, botUser, channel);
        }

    }

}
