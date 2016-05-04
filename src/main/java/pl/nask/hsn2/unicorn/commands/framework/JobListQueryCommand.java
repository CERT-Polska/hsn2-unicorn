/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.1.
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

package pl.nask.hsn2.unicorn.commands.framework;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.Info.InfoData;
import pl.nask.hsn2.protobuff.Info.InfoRequest;
import pl.nask.hsn2.protobuff.Info.InfoType;
import pl.nask.hsn2.protobuff.Jobs.JobStatus;
import pl.nask.hsn2.protobuff.Object.ObjectData;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Request;
import pl.nask.hsn2.unicorn.connector.Response;
import pl.nask.hsn2.unicorn.connector.UnicornUtils;

import com.google.protobuf.InvalidProtocolBufferException;

public class JobListQueryCommand extends JobListCommand {

	private String workflowName;

	public JobListQueryCommand(String queueName, String workflowName) throws ConnectionException {
		super(queueName);
		this.workflowName = workflowName;
	}
	
	@Override
	protected void appendJobInStatusInfo(StringBuilder sb, JobStatus status,
			Set<Long> jobIds) {
		if (jobIds == null)
			return;
		Set<Long> filteredJobIds = filterJobIds(jobIds);
		super.appendJobInStatusInfo(sb, status, filteredJobIds);
	}

	private Set<Long> filterJobIds(Set<Long> jobIds) {
		if (workflowName == null)
			return jobIds;
		
		Set<Long> result = new TreeSet<>();
		for (Long jobId: jobIds) {
			if (usesGivenWorkflow(jobId))
				result.add(jobId);
		}
		return result;
	}

	private boolean usesGivenWorkflow(Long jobId) {		
		Response response = getJobInfo(jobId);
		ObjectData data = parseObjectData(response);
		Map<String,String> attributes = UnicornUtils.getAttributesMap(data.getAttrsList());
		
		String jobWorkflowName = attributes.get("job_workflow_name");
		return workflowName.equals(jobWorkflowName);
	}

	private ObjectData parseObjectData(Response response) {
		try {
			if("InfoData".equals(response.getType())) {
				return InfoData.parseFrom(response.getBody()).getData();
			} else {
				throw new IllegalStateException("Unexpected response type: expected InfoData, got " + response.getType());
			}
		} catch (InvalidProtocolBufferException e) {			
			LOGGER.error(e.getMessage(),e);
			throw new IllegalStateException(e);
		}
	}

	private Response getJobInfo(Long jobId) {
		System.out.print(".");
		InfoRequest infoRequest = InfoRequest.newBuilder().setType(InfoType.JOB).setId(jobId).build();
		byte[] reqMsg = infoRequest.toByteArray();
		
		Request req = new Request(destinationQueueName, "InfoRequest", reqMsg);
		
		try {
			connector.send(req);
			return connector.receive();
		} catch (ConnectionException e) {
			throw new IllegalStateException(e);
		}
		
	}
	
	public static class Builder extends AbstractCommandBuilder {

		@Override
		protected Command buildCommand(CommandLineParams cmdParams, CommandLine cmd)
				throws ConnectionException {
			JobListCommand command = new JobListQueryCommand(cmdParams.getFrameworkQueueName(), cmd.getOptionValue("jlq"));
			command.setBrief(cmdParams.isBrief());
			return command;
		}
	}
}
