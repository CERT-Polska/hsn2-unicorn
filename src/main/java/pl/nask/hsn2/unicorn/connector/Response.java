/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.0.
 * 
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.nask.hsn2.unicorn.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.protobuff.Config.GetConfigReply;
import pl.nask.hsn2.protobuff.Config.SetConfigReply;
import pl.nask.hsn2.protobuff.Config.SetConfigRequest;
import pl.nask.hsn2.protobuff.Info.InfoData;
import pl.nask.hsn2.protobuff.Info.InfoError;
import pl.nask.hsn2.protobuff.Info.InfoRequest;
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
import pl.nask.hsn2.protobuff.ObjectStore.ObjectAdded;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectResponse;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectResponse.ResponseType;
import pl.nask.hsn2.protobuff.Process.TaskAccepted;
import pl.nask.hsn2.protobuff.Process.TaskCompleted;
import pl.nask.hsn2.protobuff.Process.TaskError;
import pl.nask.hsn2.protobuff.Process.TaskRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowError;
import pl.nask.hsn2.protobuff.Workflows.WorkflowGetReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowGetRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowHistoryReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowHistoryRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowListReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowListRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowPolicyReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowPolicyRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowStatusReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowStatusRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowUploadReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowUploadRequest;
import pl.nask.hsn2.unicorn.FailedCommandException;

import com.google.protobuf.InvalidProtocolBufferException;

public class Response {

    private static final Logger LOGGER = LoggerFactory.getLogger(Response.class);
    private static final String DEFAULT_CONTENT_TYPE = "application/hsn2+protobuf";
    protected String type;
    protected String contentType;
    protected byte[] body;

    public Response(String type, String contentType, byte[] body) {
        this.type = type;
        this.contentType = contentType;
        this.body = body.clone();
    }

    public String getType() {
        return type;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public String toString() {
        String msg = "type: " + type + "\n";
		if (DEFAULT_CONTENT_TYPE.equals(contentType)) {
			try {
				if ("GetConfigRequest".equals(type)) {
					// Empty message.
				} else if ("GetConfigReply".equals(type)) {
					msg += GetConfigReply.parseFrom(body).toString();
				} else if ("SetConfigRequest".equals(type)) {
					msg += SetConfigRequest.parseFrom(body).toString();
				} else if ("SetConfigReply".equals(type)) {
					msg += SetConfigReply.parseFrom(body).toString();
				} else if ("InfoRequest".equals(type)) {
					msg += InfoRequest.parseFrom(body).toString();
				} else if ("InfoData".equals(type)) {
					msg += InfoData.parseFrom(body).toString();
				} else if ("InfoError".equals(type)) {
					msg += InfoError.parseFrom(body).toString();
				} else if ("Ping".equals(type)) {
					// Empty message.
				} else if ("JobListRequest".equals(type)) {
					msg += JobListRequest.parseFrom(body).toString();
				} else if ("JobInfo".equals(type)) {
					msg += JobInfo.parseFrom(body).toString();
				} else if ("JobListReply".equals(type)) {
					msg += JobListReply.parseFrom(body).toString();
				} else if ("JobDescriptor".equals(type)) {
					msg += JobDescriptor.parseFrom(body).toString();
				} else if ("JobAccepted".equals(type)) {
					msg += JobAccepted.parseFrom(body).toString();
				} else if ("JobRejected".equals(type)) {
					msg += JobRejected.parseFrom(body).toString();
				} else if ("JobStarted".equals(type)) {
					msg += JobStarted.parseFrom(body).toString();
				} else if ("JobFinished".equals(type)) {
					msg += JobFinished.parseFrom(body).toString();
				} else if ("JobFinishedReminder".equals(type)) {
					msg += JobFinishedReminder.parseFrom(body).toString();
				} else if ("JobCancelRequest".equals(type)) {
					msg += JobCancelRequest.parseFrom(body).toString();
				} else if ("JobCancelReply".equals(type)) {
					msg += JobCancelReply.parseFrom(body).toString();
				} else if ("ObjectRequest".equals(type)) {
					msg += ObjectRequest.parseFrom(body).toString();
				} else if ("ObjectResponse".equals(type)) {
					ObjectResponse or = ObjectResponse.parseFrom(body);
					StringBuilder builder = new StringBuilder(msg).append(or.toString());
					builder.append("\ncount: ");
					if (ResponseType.SUCCESS_GET.equals(or.getType())) {
						builder.append(or.getDataCount());
					} else {
						builder.append(or.getObjectsCount());
					}
					return builder.toString();
				} else if ("ObjectAdded".equals(type)) {
					msg += ObjectAdded.parseFrom(body).toString();
				} else if ("ObjectProcessed".equals(type)) {
					// Empty message.
				} else if ("TaskRequest".equals(type)) {
					msg += TaskRequest.parseFrom(body).toString();
				} else if ("TaskAccepted".equals(type)) {
					msg += TaskAccepted.parseFrom(body).toString();
				} else if ("TaskError".equals(type)) {
					msg += TaskError.parseFrom(body).toString();
				} else if ("TaskCompleted".equals(type)) {
					msg += TaskCompleted.parseFrom(body).toString();
				} else if ("WorkflowListRequest".equals(type)) {
					msg += WorkflowListRequest.parseFrom(body).toString();
				} else if ("WorkflowListReply".equals(type)) {
					msg += WorkflowListReply.parseFrom(body).toString();
				} else if ("WorkflowStatusRequest".equals(type)) {
					msg += WorkflowStatusRequest.parseFrom(body).toString();
				} else if ("WorkflowStatusReply".equals(type)) {
					msg += WorkflowStatusReply.parseFrom(body).toString();
				} else if ("WorkflowGetRequest".equals(type)) {
					msg += WorkflowGetRequest.parseFrom(body).toString();
				} else if ("WorkflowGetReply".equals(type)) {
					msg += WorkflowGetReply.parseFrom(body).toString();
				} else if ("WorkflowPolicyRequest".equals(type)) {
					msg += WorkflowPolicyRequest.parseFrom(body).toString();
				} else if ("WorkflowPolicyReply".equals(type)) {
					msg += WorkflowPolicyReply.parseFrom(body).toString();
				} else if ("WorkflowUploadRequest".equals(type)) {
					msg += WorkflowUploadRequest.parseFrom(body).toString();
				} else if ("WorkflowUploadReply".equals(type)) {
					msg += WorkflowUploadReply.parseFrom(body).toString();
				} else if ("WorkflowHistoryRequest".equals(type)) {
					msg += WorkflowHistoryRequest.parseFrom(body).toString();
				} else if ("WorkflowHistoryReply".equals(type)) {
					msg += WorkflowHistoryReply.parseFrom(body).toString();
				} else if ("WorkflowError".equals(type)) {
					msg += WorkflowError.parseFrom(body).toString();
				} else {
					throw new IllegalStateException("Unrecognized message type!: " + type + "\n" + new String(body));
				}
			} catch (InvalidProtocolBufferException e) {
				msg += "Respones parsing error.";
				LOGGER.error(msg, e);
			}
		} else {
            LOGGER.debug("ContentType: {}, type: {}", contentType, type);
            msg = new String(body);
        }
        return msg;
    }

    public OSResponse getOSResponse() throws FailedCommandException {
        try {
            return new OSResponse(this);
        } catch (InvalidProtocolBufferException e) {
            throw new FailedCommandException("Wrong response received! ObjectResponse expected.", e);
        }
    }
}
