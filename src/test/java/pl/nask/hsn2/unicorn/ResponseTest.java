package pl.nask.hsn2.unicorn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import pl.nask.hsn2.protobuff.Config.GetConfigReply;
import pl.nask.hsn2.protobuff.Config.GetConfigRequest;
import pl.nask.hsn2.protobuff.Config.Property;
import pl.nask.hsn2.protobuff.Config.SetConfigReply;
import pl.nask.hsn2.protobuff.Config.SetConfigRequest;
import pl.nask.hsn2.protobuff.Info.InfoData;
import pl.nask.hsn2.protobuff.Info.InfoError;
import pl.nask.hsn2.protobuff.Info.InfoRequest;
import pl.nask.hsn2.protobuff.Info.InfoType;
import pl.nask.hsn2.protobuff.Info.Ping;
import pl.nask.hsn2.protobuff.Jobs.JobAccepted;
import pl.nask.hsn2.protobuff.Jobs.JobCancelReply;
import pl.nask.hsn2.protobuff.Jobs.JobCancelRequest;
import pl.nask.hsn2.protobuff.Jobs.JobDescriptor;
import pl.nask.hsn2.protobuff.Jobs.JobFinished;
import pl.nask.hsn2.protobuff.Jobs.JobFinishedReminder;
import pl.nask.hsn2.protobuff.Jobs.JobInfo;
import pl.nask.hsn2.protobuff.Jobs.JobListReply;
import pl.nask.hsn2.protobuff.Jobs.JobListRequest;
import pl.nask.hsn2.protobuff.Jobs.JobRejected;
import pl.nask.hsn2.protobuff.Jobs.JobStarted;
import pl.nask.hsn2.protobuff.Jobs.JobStatus;
import pl.nask.hsn2.protobuff.Object.Attribute;
import pl.nask.hsn2.protobuff.Object.Attribute.Type;
import pl.nask.hsn2.protobuff.Object.ObjectData;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectAdded;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectProcessed;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.QueryStructure;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.QueryStructure.QueryType;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.RequestType;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectResponse;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectResponse.ResponseType;
import pl.nask.hsn2.protobuff.Process.TaskAccepted;
import pl.nask.hsn2.protobuff.Process.TaskCompleted;
import pl.nask.hsn2.protobuff.Process.TaskError;
import pl.nask.hsn2.protobuff.Process.TaskError.ReasonType;
import pl.nask.hsn2.protobuff.Process.TaskRequest;
import pl.nask.hsn2.protobuff.Service.Parameter;
import pl.nask.hsn2.protobuff.Service.ServiceConfig;
import pl.nask.hsn2.protobuff.Workflows.WorkflowBasicInfo;
import pl.nask.hsn2.protobuff.Workflows.WorkflowError;
import pl.nask.hsn2.protobuff.Workflows.WorkflowGetReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowGetRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowHistoryReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowHistoryRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowListReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowListRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowPolicyReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowPolicyRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowRevisionInfo;
import pl.nask.hsn2.protobuff.Workflows.WorkflowStatusReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowStatusRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowUploadReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowUploadRequest;
import pl.nask.hsn2.unicorn.connector.Response;

import com.google.protobuf.GeneratedMessage;

public class ResponseTest {
	private static final String TEST_WARNING = "test-warning-";
	private static final String TEST_VERSION = "test-version";
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseTest.class);
	private static final String TEST_SERVICE_LABEL = "test-service-label";
	private static final String TEST_VALUE = "test-value-";
	private static final String TEST_ATTRIBUTE = "test-attribute-";
	private static final String TEST_NAME = "test-name-";
	private static final String TEST_DESCRIPTION = "test-description-";
	private static final String TEST_REVISION = "test-revision-";
	private static final String TEST_ERROR = "test-error-";
	private static final String TEST_REASON = "test-reason-";
	private static final String CONTENT_TYPE = "application/hsn2+protobuf";
	private static final String WORKFLOW_CONTENT = "workflow-content-";

	/**
	 * Test is checking if all messages needed are supported by Response. If not, Response will throw exception and test
	 * fail.
	 */
	@Test
	public void allMsgTypesTest() {
		GeneratedMessage protobufMsg;
		long longId = 10000L;
		int intId = 1;

		// Initial data used in protobuf messages.
		Property testProperty = Property.newBuilder().setName(TEST_NAME + (intId++)).setValue(TEST_VALUE + (intId++)).build();
		Attribute attrBool = Attribute.newBuilder().setName(TEST_ATTRIBUTE + (intId++)).setType(Type.BOOL).setDataBool(true).build();
		Attribute attrString = Attribute.newBuilder().setName(TEST_ATTRIBUTE + (intId++)).setType(Type.STRING)
				.setDataString(TEST_VALUE + (intId++)).build();
		Attribute attrFloat = Attribute.newBuilder().setName(TEST_ATTRIBUTE + (intId++)).setType(Type.FLOAT).setDataFloat(1.23f).build();
		Attribute attrTime = Attribute.newBuilder().setName(TEST_ATTRIBUTE + (intId++)).setType(Type.TIME).setDataTime(longId++).build();
		ObjectData objectData1 = ObjectData.newBuilder().setId(longId++).addAttrs(attrBool).addAttrs(attrFloat).build();
		ObjectData objectData2 = ObjectData.newBuilder().setId(longId++).addAttrs(attrString).addAttrs(attrTime).build();
		JobInfo jobInfo1 = JobInfo.newBuilder().setId(longId++).setStatus(JobStatus.ACCEPTED).build();
		JobInfo jobInfo2 = JobInfo.newBuilder().setId(longId++).setStatus(JobStatus.FAILED).build();
		Parameter testParam1 = Parameter.newBuilder().setName(TEST_NAME + (intId++)).setValue(TEST_VALUE + longId++).build();
		Parameter testParam2 = Parameter.newBuilder().setName(TEST_NAME + (intId++)).setValue(TEST_VALUE + longId++).build();
		ServiceConfig config = ServiceConfig.newBuilder().setServiceLabel(TEST_SERVICE_LABEL + (intId++)).addParameters(testParam1)
				.addParameters(testParam2).build();
		QueryStructure query1 = QueryStructure.newBuilder().setType(QueryType.BY_ATTR_NAME).setAttrName(TEST_NAME + (intId++))
				.setAttrValue(attrBool).setNegate(true).build();
		QueryStructure query2 = QueryStructure.newBuilder().setType(QueryType.BY_ATTR_VALUE).setAttrName(TEST_NAME + (intId++))
				.setAttrValue(attrFloat).setNegate(false).build();
		WorkflowBasicInfo workflowBasicInfo1 = WorkflowBasicInfo.newBuilder().setName(TEST_NAME + (intId++)).setEnabled(false).build();
		WorkflowBasicInfo workflowBasicInfo2 = WorkflowBasicInfo.newBuilder().setName(TEST_NAME + (intId++)).setEnabled(true).build();
		WorkflowRevisionInfo workflowRevisionInfo1 = WorkflowRevisionInfo.newBuilder().setRevision(TEST_REVISION + (intId++))
				.setMtime(longId++).build();
		WorkflowRevisionInfo workflowRevisionInfo2 = WorkflowRevisionInfo.newBuilder().setRevision(TEST_REVISION + (intId++))
				.setMtime(longId++).build();

		// GetConfigRequest
		protobufMsg = GetConfigRequest.getDefaultInstance();
		doTestMsgAction(protobufMsg);

		// GetConfigReply
		protobufMsg = GetConfigReply.newBuilder().addProperties(testProperty).addProperties(testProperty).addProperties(testProperty)
				.build();
		doTestMsgAction(protobufMsg);

		// SetConfigRequest
		protobufMsg = SetConfigRequest.newBuilder().addProperties(testProperty).setReplace(false).build();
		doTestMsgAction(protobufMsg);

		// SetConfigReply
		protobufMsg = SetConfigReply.newBuilder().setSuccess(true).build();
		doTestMsgAction(protobufMsg);

		// InfoRequest
		protobufMsg = InfoRequest.newBuilder().setType(InfoType.JOB).setId(longId++).build();
		doTestMsgAction(protobufMsg);

		// InfoData
		protobufMsg = InfoData.newBuilder().setType(InfoType.JOB).setData(objectData1).build();
		doTestMsgAction(protobufMsg);

		// InfoError
		protobufMsg = InfoError.newBuilder().setReason(TEST_REASON + (intId++)).setType(InfoType.JOB).build();
		doTestMsgAction(protobufMsg);

		// Ping
		protobufMsg = Ping.getDefaultInstance();
		doTestMsgAction(protobufMsg);

		// JobListRequest
		protobufMsg = JobListRequest.getDefaultInstance();
		doTestMsgAction(protobufMsg);

		// JobInfo
		protobufMsg = JobInfo.newBuilder().setId(longId++).setStatus(JobStatus.ACCEPTED).build();
		doTestMsgAction(protobufMsg);

		// JobListReply
		protobufMsg = JobListReply.newBuilder().addJobs(jobInfo1).addJobs(jobInfo2).build();
		doTestMsgAction(protobufMsg);

		// JobDescriptor
		protobufMsg = JobDescriptor.newBuilder().setWorkflow(TEST_NAME + (intId++)).setVersion(TEST_VERSION + (intId++)).addConfig(config)
				.build();
		doTestMsgAction(protobufMsg);

		// JobAccepted
		protobufMsg = JobAccepted.newBuilder().setJob(longId++).build();
		doTestMsgAction(protobufMsg);

		// JobRejected
		protobufMsg = JobRejected.newBuilder().setReason(TEST_REASON + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// JobStarted
		protobufMsg = JobStarted.newBuilder().setJob(longId++).build();
		doTestMsgAction(protobufMsg);

		// JobFinished
		protobufMsg = JobFinished.newBuilder().setJob(longId++).setStatus(JobStatus.CANCELLED).build();
		doTestMsgAction(protobufMsg);

		// JobFinishedReminder
		protobufMsg = JobFinishedReminder.newBuilder().setJob(longId++).setOffendingTask(intId++).setStatus(JobStatus.FAILED).build();
		doTestMsgAction(protobufMsg);

		// JobCancelRequest
		protobufMsg = JobCancelRequest.newBuilder().setJob(longId++).build();
		doTestMsgAction(protobufMsg);

		// JobCancelReply
		protobufMsg = JobCancelReply.newBuilder().setCancelled(true).setReason(TEST_REASON + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// ObjectRequest
		protobufMsg = ObjectRequest.newBuilder().setType(RequestType.GET).setJob(longId++).addObjects(longId++).addObjects(longId++)
				.addObjects(longId++).addData(objectData1).addData(objectData2).setTaskId(intId++).setOverwrite(false).addQuery(query1)
				.addQuery(query2).build();
		doTestMsgAction(protobufMsg);

		// ObjectResponse
		protobufMsg = ObjectResponse.newBuilder().setType(ResponseType.SUCCESS_QUERY).addData(objectData1).addData(objectData2)
				.addObjects(longId++).addObjects(longId++).addObjects(longId++).addObjects(longId++).addMissing(longId++)
				.addMissing(longId++).addMissing(longId++).addConflicts(longId++).addConflicts(longId++).addConflicts(longId++)
				.setError(TEST_ERROR + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// ObjectAdded
		protobufMsg = ObjectAdded.newBuilder().setJob(longId++).setTaskId(intId++).addObjects(longId++).addObjects(longId++)
				.addObjects(longId++).addObjects(longId++).addObjects(longId++).addObjects(longId++).build();
		doTestMsgAction(protobufMsg);

		// ObjectProcessed
		protobufMsg = ObjectProcessed.getDefaultInstance();
		doTestMsgAction(protobufMsg);

		// TaskRequest
		protobufMsg = TaskRequest.newBuilder().setTaskId(intId++).addParameters(testParam1).addParameters(testParam2).setJob(longId++)
				.setObject(longId++).build();
		doTestMsgAction(protobufMsg);

		// TaskAccepted
		protobufMsg = TaskAccepted.newBuilder().setJob(longId++).setTaskId(intId++).build();
		doTestMsgAction(protobufMsg);

		// TaskError
		protobufMsg = TaskError.newBuilder().setJob(longId++).setTaskId(intId++).setReason(ReasonType.OBJ_STORE)
				.setDescription(TEST_DESCRIPTION + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// TaskCompleted
		protobufMsg = TaskCompleted.newBuilder().setJob(longId++).setTaskId(intId++).addWarnings(TEST_WARNING + (intId++))
				.addWarnings(TEST_WARNING + (intId++)).addObjects(longId++).addObjects(longId++).addObjects(longId++).addObjects(longId++)
				.build();
		doTestMsgAction(protobufMsg);

		// WorkflowListRequest
		protobufMsg = WorkflowListRequest.newBuilder().setEnabledOnly(true).build();
		doTestMsgAction(protobufMsg);

		// WorkflowListReply
		protobufMsg = WorkflowListReply.newBuilder().addWorkflows(workflowBasicInfo1).addWorkflows(workflowBasicInfo2).build();
		doTestMsgAction(protobufMsg);

		// WorkflowStatusRequest
		protobufMsg = WorkflowStatusRequest.newBuilder().setName(TEST_NAME + (intId++)).setRevision(TEST_REVISION + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// WorkflowStatusReply
		protobufMsg = WorkflowStatusReply.newBuilder().setValid(false).setEnabled(true).setInfo(workflowRevisionInfo1)
				.setDescription(TEST_DESCRIPTION + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// WorkflowGetRequest
		protobufMsg = WorkflowGetRequest.newBuilder().setName(TEST_NAME + (intId++)).setRevision(TEST_REVISION + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// WorkflowGetReply
		protobufMsg = WorkflowGetReply.newBuilder().setContent(WORKFLOW_CONTENT + (intId++)).setRevision(TEST_REVISION + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// WorkflowPolicyRequest
		protobufMsg = WorkflowPolicyRequest.newBuilder().setName(TEST_NAME + (intId++)).setEnabled(true).build();
		doTestMsgAction(protobufMsg);

		// WorkflowPolicyReply
		protobufMsg = WorkflowPolicyReply.newBuilder().setPrevious(false).build();
		doTestMsgAction(protobufMsg);

		// WorkflowUploadRequest
		protobufMsg = WorkflowUploadRequest.newBuilder().setName(TEST_NAME + (intId++)).setContent(WORKFLOW_CONTENT + (intId++))
				.setOverwrite(true).build();
		doTestMsgAction(protobufMsg);

		// WorkflowUploadReply
		protobufMsg = WorkflowUploadReply.newBuilder().setRevision(TEST_REVISION + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// WorkflowHistoryRequest
		protobufMsg = WorkflowHistoryRequest.newBuilder().setName(TEST_NAME + (intId++)).build();
		doTestMsgAction(protobufMsg);

		// WorkflowHistoryReply
		protobufMsg = WorkflowHistoryReply.newBuilder().addHistory(workflowRevisionInfo1).addHistory(workflowRevisionInfo2).build();
		doTestMsgAction(protobufMsg);

		// WorkflowError
		protobufMsg = WorkflowError.newBuilder().setReason(TEST_REASON + (intId++)).build();
		doTestMsgAction(protobufMsg);

		LOGGER.info("Test passed.");
	}

	private void doTestMsgAction(GeneratedMessage protobufMsg) {
		String msgType = protobufMsg.getClass().getSimpleName();
		Response response = new Response(msgType, CONTENT_TYPE, protobufMsg.toByteArray());
		String rspToString = response.toString();
		LOGGER.debug("Msg = {}", rspToString);
	}
}
