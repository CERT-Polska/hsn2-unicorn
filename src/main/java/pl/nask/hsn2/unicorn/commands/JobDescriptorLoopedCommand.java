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

package pl.nask.hsn2.unicorn.commands;

import java.util.Set;
import java.util.TreeSet;

import pl.nask.hsn2.protobuff.Info.InfoData;
import pl.nask.hsn2.protobuff.Info.InfoError;
import pl.nask.hsn2.protobuff.Info.InfoRequest;
import pl.nask.hsn2.protobuff.Info.InfoType;
import pl.nask.hsn2.protobuff.Jobs.JobAccepted;
import pl.nask.hsn2.protobuff.Jobs.JobDescriptor;
import pl.nask.hsn2.protobuff.Jobs.JobRejected;
import pl.nask.hsn2.protobuff.Object.Attribute;
import pl.nask.hsn2.protobuff.Object.ObjectData;
import pl.nask.hsn2.protobuff.Service.Parameter;
import pl.nask.hsn2.protobuff.Service.ServiceConfig;
import pl.nask.hsn2.unicorn.FailedCommandException;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Request;
import pl.nask.hsn2.unicorn.connector.Response;

import com.google.protobuf.InvalidProtocolBufferException;

public class JobDescriptorLoopedCommand extends AbstractCommand {
	private Long jobId;
	private String workflowName;
	private long loopCount;
	private final boolean isLoopCountEnabled;
	private final String osQueueName;
	private final String fwQueueName;

	public JobDescriptorLoopedCommand(String fwQueueName, String osQueueName, String workflowName, Long jobId, long loopCount)
			throws ConnectionException {
		this.fwQueueName = fwQueueName;
		this.osQueueName = osQueueName;
		this.workflowName = workflowName;
		this.jobId = jobId;
		if (loopCount > 0) {
			this.loopCount = loopCount;
			this.isLoopCountEnabled = true;
		} else {
			this.loopCount = 1;
			this.isLoopCountEnabled = false;
		}
		connector.connectRPC();
	}

	public JobDescriptorLoopedCommand(String fwQueueName, String osQueueName, String workflowName, long loopCount)
			throws ConnectionException {
		this(fwQueueName, osQueueName, workflowName, null, loopCount);
		LOGGER.info("Created JobDescriptorLooped command.\nOS queue name = {}\nWorkflow = {}\nLoop count = {}", new Object[] { osQueueName,
				workflowName, loopCount > 0 ? "" + loopCount : "infinite" });
	}

	public JobDescriptorLoopedCommand(String fwQueueName, String osQueueName, long jobId, long loopCount) throws ConnectionException {
		this(fwQueueName, osQueueName, null, jobId, loopCount);
		LOGGER.info("Created JobDescriptorLooped command.\nOS queue name = {}\nJob id = {}\nLoop count = {}", new Object[] { osQueueName,
				jobId, loopCount > 0 ? "" + loopCount : "infinite" });
	}

	@Override
	public void execute() throws ConnectionException, FailedCommandException {
		// At first check if we are to start new job or only to monitor existing one.
		if (workflowName != null) {
			// Workflow name provided, that means we have to start new job first.
			try {
				jobId = startNewJob(workflowName);
			} catch (IllegalStateException e) {
				LOGGER.debug("Can't start new job, rejected.\n{}", e);
				throw new FailedCommandException("Can't start new job, rejected.");
			}
		}

		// Monitor current job status.
		while (loopCount > 0) {
			LOGGER.info("==========");

			// Check for job status.
			checkForJobStatus(jobId);

			// Check if job is completed.

			// If job is completed, start new one.

			// Update loop count.
			if (isLoopCountEnabled) {
				loopCount--;
			}

			// Go to sleep.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Should never happen.
				LOGGER.debug("Sleep interrupted.");
			}
		}
	}

	/**
	 * Starts new job. Returns id of started job.
	 * 
	 * @param workflowName
	 * @return Started job id.
	 * @throws ConnectionException
	 *             In case of connection issues.
	 * @throws IllegalStateException
	 *             When job is rejected.
	 */
	private long startNewJob(String workflowName) throws ConnectionException {
		Request request = new Request(fwQueueName, "JobDescriptor", buildJobDescriptorMessage());
		connector.send(request);
		Response response = connector.receive();

		long jobId = 0;
		try {
			if ("JobAccepted".equals(response.getType())) {
				JobAccepted ja = JobAccepted.parseFrom(response.getBody());
				jobId = ja.getJob();
			} else if ("JobRejected".equals(response.getType())) {
				JobRejected jr = JobRejected.parseFrom(response.getBody());
				throw new IllegalStateException("Job rejected. " + jr.getReason());
			} else {
				// Should never happen.
				throw new RuntimeException("Wrong message type received as an answer.");
			}
		} catch (InvalidProtocolBufferException e) {
			// Should never happen.
			throw new RuntimeException("Exception while deserializing message.", e);
		}

		LOGGER.info("New job started, id={}", jobId);
		return jobId;
	}

	private void checkForJobStatus(long jobId) throws ConnectionException, FailedCommandException {
		LOGGER.info("Check job info, id={}", jobId);
		InfoRequest infoRequest = InfoRequest.newBuilder().setType(InfoType.JOB).setId(jobId).build();
		Request request = new Request(fwQueueName, "InfoRequest", infoRequest.toByteArray());
		connector.send(request);
		Response response = connector.receive();

		// WST poprawic wyswietlanie
		displayResults(response);
	}

	private byte[] buildJobDescriptorMessage() {
		JobDescriptor.Builder jobDescriptorBuilder = JobDescriptor.newBuilder().setWorkflow(workflowName);

		// WST dodac obsluge service parameters, usunac nastepny wiersz
		String[] serviceParams = {};

		// Add service parameters.
		for (String param : serviceParams) {
			jobDescriptorBuilder.addConfig(prepareServiceConfig(param));
		}

		return jobDescriptorBuilder.build().toByteArray();
	}

	private ServiceConfig.Builder prepareServiceConfig(String param) {
		int commaSign = param.indexOf('.');
		int equalSign = param.indexOf('=');
		String serviceName = param.substring(0, commaSign);
		String paramName = param.substring(commaSign + 1, equalSign);
		String paramValue = param.substring(equalSign + 1);
		ServiceConfig.Builder configBuilder = ServiceConfig.newBuilder().setServiceLabel(serviceName)
				.addParameters(Parameter.newBuilder().setName(paramName).setValue(paramValue));
		return configBuilder;
	}

	protected void displayResults(Response response) {
		String type = response.getType();
		StringBuilder displayResults = new StringBuilder("JOB INFO\n");
		if ("InfoData".equals(type)) {
			try {
				Set<String> displayValues = new TreeSet<>();
				InfoData data = InfoData.parseFrom(response.getBody());
				ObjectData objData = data.getData();
				for (Attribute attr : objData.getAttrsList()) {
					displayValues.add(attr.getName() + " = " + getValueStringRepresentation(attr));
				}
				for (String s : displayValues) {
					displayResults.append(s).append("\n");
				}
			} catch (InvalidProtocolBufferException e) {
				// Should never happen.
				displayResults.append("Could not deserialize response message").append(e);
			}
		} else if ("InfoError".equals(type)) {
			try {
				InfoError error = InfoError.parseFrom(response.getBody());
				displayResults.append("Could not get job info: ").append(error.getReason());
			} catch (InvalidProtocolBufferException e) {
				// Should never happen.
				displayResults.append("Could not deserialize response message").append(e);
			}
		} else {
			displayResults.append("WRONG MSG TYPE: ").append(type);
		}
		LOGGER.info(displayResults.toString());
	}

	private String getValueStringRepresentation(final Attribute a) {
		String s = "";
		switch (a.getType()) {
		case BOOL:
			s += a.getDataBool();
			break;
		case INT:
			s += a.getDataInt();
			break;
		case TIME:
			s += a.getDataTime();
			break;
		case FLOAT:
			s += a.getDataFloat();
			break;
		case STRING:
			s += a.getDataString();
			break;
		case OBJECT:
			s += a.getDataObject();
			break;
		case BYTES:
			s += a.getDataBytes();
			break;
		default:
			break;
		}
		return s;
	}
}
