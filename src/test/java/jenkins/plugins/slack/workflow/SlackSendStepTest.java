package jenkins.plugins.slack.workflow;

import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.Messages;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.SlackService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Traditional Unit tests, allows testing null Jenkins.getInstance()
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class,SlackSendStep.class})
public class SlackSendStepTest {

    @Mock
    TaskListener taskListenerMock;
    @Mock
    PrintStream printStreamMock;
    @Mock
    PrintWriter printWriterMock;
    @Mock
    StepContext stepContextMock;
    @Mock
    SlackService slackServiceMock;
    @Mock
    Jenkins jenkins;
    @Mock
    SlackNotifier.DescriptorImpl slackDescMock;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        when(jenkins.getDescriptorByType(SlackNotifier.DescriptorImpl.class)).thenReturn(slackDescMock);
    }

    @Test
    public void testStepOverrides() throws Exception {
        SlackSendStep slackSendStep = new SlackSendStep("message");
        slackSendStep.setToken("token");
        slackSendStep.setTokenCredentialId("tokenCredentialId");
        slackSendStep.setBotUser(false);
        slackSendStep.setBaseUrl("baseUrl/");
        slackSendStep.setTeamDomain("teamDomain");
        slackSendStep.setChannel("channel");
        slackSendStep.setColor("good");
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(slackSendStep, stepContextMock));

        when(Jenkins.getInstance()).thenReturn(jenkins);

        when(stepContextMock.get(TaskListener.class)).thenReturn(taskListenerMock);

        when(slackDescMock.getToken()).thenReturn("differentToken");
        when(slackDescMock.getBotUser()).thenReturn(true);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);
        when(slackServiceMock.publish(anyString(), anyString())).thenReturn(true);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService("baseUrl/", "teamDomain", "token", "tokenCredentialId", false, "channel");
        verify(slackServiceMock, times(1)).publish("message", "good");
        assertFalse(slackSendStep.isFailOnError());
    }

    @Test
    public void testStepOverrides2() throws Exception {
        SlackSendStep slackSendStep = new SlackSendStep("message");
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(slackSendStep, stepContextMock));
        slackSendStep.setToken("token");
        slackSendStep.setTokenCredentialId("tokenCredentialId");
        slackSendStep.setBotUser(false);
        slackSendStep.setBaseUrl("baseUrl");
        slackSendStep.setTeamDomain("teamDomain");
        slackSendStep.setChannel("channel");
        slackSendStep.setColor("good");

        when(Jenkins.getInstance()).thenReturn(jenkins);

        when(stepContextMock.get(TaskListener.class)).thenReturn(taskListenerMock);

        when(slackDescMock.getToken()).thenReturn("differentToken");
        when(slackDescMock.getBotUser()).thenReturn(true);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);
        when(slackServiceMock.publish(anyString(), anyString())).thenReturn(true);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService("baseUrl/", "teamDomain", "token", "tokenCredentialId", false, "channel");
        verify(slackServiceMock, times(1)).publish("message", "good");
        assertFalse(slackSendStep.isFailOnError());
    }

    @Test
    public void testStepWithAttachments() throws Exception {
        SlackSendStep slackSendStep = new SlackSendStep("message");
        JSONArray attachments = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title","Title of the message");
        jsonObject.put("author_name","Name of the author");
        jsonObject.put("author_icon","Avatar for author");
        attachments.add(jsonObject);
        slackSendStep.setAttachments(attachments.toString());
        ((JSONObject) attachments.get(0)).put("fallback", "message");
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(slackSendStep, stepContextMock));

        when(Jenkins.getInstance()).thenReturn(jenkins);
        when(stepContextMock.get(TaskListener.class)).thenReturn(taskListenerMock);


        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(0)).publish("message", "");
        verify(slackServiceMock, times(1)).publish(attachments, "");

    }

    @Test
    public void testValuesForGlobalConfig() throws Exception {

        SlackSendStep slackSendStep = new SlackSendStep("message");
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(slackSendStep, stepContextMock));

        when(Jenkins.getInstance()).thenReturn(jenkins);

        when(stepContextMock.get(TaskListener.class)).thenReturn(taskListenerMock);

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getToken()).thenReturn("globalToken");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.getBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService("globalBaseUrl", "globalTeamDomain", "globalToken", "globalTokenCredentialId", false, "globalChannel");
        verify(slackServiceMock, times(1)).publish("message", "");
        assertNull(slackSendStep.getBaseUrl());
        assertNull(slackSendStep.getTeamDomain());
        assertNull(slackSendStep.getToken());
        assertNull(slackSendStep.getTokenCredentialId());
        assertNull(slackSendStep.getChannel());
        assertNull(slackSendStep.getColor());
    }

    @Test
    public void testNonNullEmptyColor() throws Exception {

        SlackSendStep slackSendStep = new SlackSendStep("message");
        slackSendStep.setColor("");
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(slackSendStep, stepContextMock));

        when(Jenkins.getInstance()).thenReturn(jenkins);

        when(stepContextMock.get(TaskListener.class)).thenReturn(taskListenerMock);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(1)).publish("message", "");
        assertNull(slackSendStep.getColor());
    }

    @Test
    public void testNullJenkinsInstance() throws Exception {

        SlackSendStep slackSendStep = new SlackSendStep("message");
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(slackSendStep, stepContextMock));

        when(Jenkins.getInstance()).thenThrow(NullPointerException.class);

        when(stepContextMock.get(TaskListener.class)).thenReturn(taskListenerMock);

        when(taskListenerMock.error(anyString())).thenReturn(printWriterMock);
        doNothing().when(printStreamMock).println();

        stepExecution.run();
        verify(taskListenerMock, times(1)).error(Messages.NotificationFailedWithException(anyString()));
    }
}
