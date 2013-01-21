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
import pl.nask.hsn2.protobuff.Info.InfoData;
import pl.nask.hsn2.protobuff.Info.InfoError;
import pl.nask.hsn2.protobuff.Jobs.JobAccepted;
import pl.nask.hsn2.protobuff.Jobs.JobDescriptor;
import pl.nask.hsn2.protobuff.Jobs.JobListReply;
import pl.nask.hsn2.protobuff.Jobs.JobRejected;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectResponse;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectResponse.ResponseType;
import pl.nask.hsn2.protobuff.Process.TaskAccepted;
import pl.nask.hsn2.protobuff.Process.TaskCompleted;
import pl.nask.hsn2.protobuff.Process.TaskError;
import pl.nask.hsn2.protobuff.Process.TaskRequest;
import pl.nask.hsn2.protobuff.Workflows.WorkflowError;
import pl.nask.hsn2.protobuff.Workflows.WorkflowGetReply;
import pl.nask.hsn2.protobuff.Workflows.WorkflowListReply;
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
        String msg;
        if (DEFAULT_CONTENT_TYPE.equals(contentType)) {
            try {
                if ("JobAccepted".equals(type)) {
                    msg = JobAccepted.parseFrom(body).toString();
                } else if ("JobListReply".equals(type)) {
                    msg = JobListReply.parseFrom(body).toString();
                } else if ("InfoData".equals(type)) {
                    msg = InfoData.parseFrom(body).toString();
                } else if ("InfoError".equals(type)) {
                    msg = InfoError.parseFrom(body).toString();
                } else if ("JobRejected".equals(type)) {
                    msg = JobRejected.parseFrom(body).toString();
                } else if ("WorkflowListReply".equals(type)) {
                    msg = WorkflowListReply.parseFrom(body).toString();
                } else if ("WorkflowGetReply".equals(type)) {
                    msg = WorkflowGetReply.parseFrom(body).toString();
                } else if ("GetConfigReply".equals(type)) {
                    msg = GetConfigReply.parseFrom(body).toString();
                } else if ("TaskRequest".equals(type)) {
                    msg = TaskRequest.parseFrom(body).toString();
                } else if ("TaskError".equals(type)) {
                    msg = TaskError.parseFrom(body).toString();
                } else if ("TaskAccepted".equals(type)) {
                    msg = TaskAccepted.parseFrom(body).toString();
                } else if ("TaskCompleted".equals(type)) {
                    msg = TaskCompleted.parseFrom(body).toString();
                } else if ("ObjectResponse".equals(type)) {
                    ObjectResponse or = ObjectResponse.parseFrom(body);

                    StringBuilder builder = new StringBuilder(or.toString());
                    builder.append("\ncount: ");
                    if (ResponseType.SUCCESS_GET.equals(or.getType())) {
                        builder.append(or.getDataCount());
                    } else {
                        builder.append(or.getObjectsCount());
                    }
                    return builder.toString();
                } else if ("JobDescriptor".equals(type)) {
                    msg = JobDescriptor.parseFrom(body).toString();
                } else if ("WorkflowError".equals(type)) {
                    msg = WorkflowError.parseFrom(body).toString();
                } else {
                    throw new IllegalStateException("Unrecognized message type!: " + type + "\n" + new String(body));
                }
            } catch (InvalidProtocolBufferException e) {
                msg = "Respones parsing error.";
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
            OSResponse osResponse = new OSResponse(this);
            return osResponse;
        } catch (InvalidProtocolBufferException e) {
            throw new FailedCommandException("Wrong response received! ObjectResponse expected.", e);
        }
    }
}
