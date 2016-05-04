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

package pl.nask.hsn2.unicorn.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import pl.nask.hsn2.protobuff.Info.InfoData;
import pl.nask.hsn2.protobuff.Info.InfoError;
import pl.nask.hsn2.protobuff.Info.InfoRequest;
import pl.nask.hsn2.protobuff.Info.InfoType;
import pl.nask.hsn2.protobuff.Jobs.JobAccepted;
import pl.nask.hsn2.protobuff.Jobs.JobDescriptor;
import pl.nask.hsn2.protobuff.Jobs.JobRejected;
import pl.nask.hsn2.protobuff.Object.Attribute;
import pl.nask.hsn2.protobuff.Object.ObjectData;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.FailedCommandException;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.JobErrorException;
import pl.nask.hsn2.unicorn.connector.Request;
import pl.nask.hsn2.unicorn.connector.Response;
import pl.nask.hsn2.unicorn.connector.UnicornUtils;

import com.google.protobuf.InvalidProtocolBufferException;

public class JobDescriptorLoopedCommand extends AbstractCommand {
	private Long jobId;
	private String workflowName;
	private long loopCount;
	private final String fwQueueName;
	private Map<String, String> previousDisplayValues;
	private String[] serviceParams = null;
	/**
	 * Sleep time in miliseconds.
	 */
	private final long sleepTime;

	private JobDescriptorLoopedCommand(String fwQueueName, String workflowName, Long jobId, long loopCount, Long sleepTime)
			throws ConnectionException {
		if (jobId != null && jobId < 1) {
			throw new RuntimeException("Job id should be positive number.");
		}
		if (loopCount < 1) {
			throw new RuntimeException("Loop count id should be positive number.");
		}
		if (sleepTime != null && sleepTime < 1) {
			throw new RuntimeException("Sleep time id should be positive number.");
		}

		this.fwQueueName = fwQueueName;
		this.workflowName = workflowName;
		this.jobId = jobId;
		this.loopCount = loopCount;
		this.sleepTime = (sleepTime == null ? 10 : sleepTime) * 1000;
		connector.connectRPC();
		LOGGER.info(
				"Created JobDescriptorLooped command.\nQueue name = {}\nWorkflow name = {}\nJob id = {}\nLoop count = {}\nSleep time = {} sec.\n",
				new Object[] { fwQueueName, workflowName == null ? "n/a" : workflowName, jobId == null ? "n/a" : jobId, loopCount,
						sleepTime == null ? "10" : sleepTime });
	}

	public JobDescriptorLoopedCommand(String fwQueueName, String workflowName, long loopCount, Long sleepTime) throws ConnectionException {
		this(fwQueueName, workflowName, null, loopCount, sleepTime);
	}

	public JobDescriptorLoopedCommand(String fwQueueName, long jobId, long loopCount, Long sleepTime) throws ConnectionException {
		this(fwQueueName, null, jobId, loopCount, sleepTime);
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
			// Check for job status.
			boolean isJobFinished = false;
			try {
				isJobFinished = checkForJobStatus(jobId);
			} catch (InvalidProtocolBufferException e1) {
				LOGGER.info("Couldn't parse protobuf message", e1);
				break;
			} catch (JobErrorException e) {
				LOGGER.info("Job doesn't exist.", e);
				break;
			}

			// Check if job is completed.
			if (isJobFinished) {
				// Update loop count.
				loopCount--;
				if (loopCount == 0) {
					break;
				}

				// Start new job.
				try {
					jobId = startNewJob(workflowName);
				} catch (IllegalStateException e) {
					LOGGER.debug("Can't start new job, rejected.\n{}", e);
					throw new FailedCommandException("Can't start new job, rejected.");
				}
			}

			// Go to sleep.
			try {
				Thread.sleep(sleepTime);
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

	private boolean checkForJobStatus(long jobId) throws ConnectionException, FailedCommandException, InvalidProtocolBufferException,
			JobErrorException {
		InfoRequest infoRequest = InfoRequest.newBuilder().setType(InfoType.JOB).setId(jobId).build();
		Request request = new Request(fwQueueName, "InfoRequest", infoRequest.toByteArray());
		connector.send(request);
		Response response = connector.receive();

		// Extract data from response.
		boolean isJobFinished = false;
		String type = response.getType();
		Map<String, String> displayValues = new TreeMap<>();
		displayValues.put("_job_id", "" + jobId);
		if ("InfoData".equals(type)) {
			InfoData data = InfoData.parseFrom(response.getBody());
			ObjectData objData = data.getData();
			for (Attribute attr : objData.getAttrsList()) {
				String attrName = attr.getName();
				String attrValue = UnicornUtils.getValueStringRepresentation(attr);
				if (workflowName == null && "job_workflow_name".equals(attrName)) {
					workflowName = attrValue;
				}
				if (serviceParams == null && "job_custom_params".equals(attrName)) {
					serviceParams = parseServiceParameters(attrValue);
				}
				displayValues.put(attrName, attrValue);
				if ("job_status".equals(attrName)
						&& ("COMPLETED".equals(attrValue) || "FAILED".equals(attrValue) || "CANCELLED".equals(attrValue))) {
					isJobFinished = true;
				}
			}
		} else if ("InfoError".equals(type)) {
			InfoError err = InfoError.parseFrom(response.getBody());
			throw new JobErrorException("Job error: " + err.getReason());
		} else {
			displayValues.put("WRONG MSG TYPE", type);
		}
		displayResults(displayValues, !isJobFinished);
		return isJobFinished;
	}

	private String[] parseServiceParameters(String parametersAsString) {
		if ("{}".equals(parametersAsString)) {
			return new String[] {};
		}

		List<String> params = new ArrayList<>();
		JSONObject jsonServices = (JSONObject) JSONValue.parse(parametersAsString);
		Iterator it = jsonServices.entrySet().iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof Entry) {
				Entry entry = (Entry) obj;
				String serviceName = (String) entry.getKey();
				JSONObject jsonServiceParams = (JSONObject) entry.getValue();
				Iterator it2 = jsonServiceParams.entrySet().iterator();
				while (it2.hasNext()) {
					Object obj2 = it2.next();
					if (obj2 instanceof Entry) {
						Entry entry2 = (Entry) obj2;
						String paramName = (String) entry2.getKey();
						String paramValue = (String) entry2.getValue();
						params.add(serviceName + "." + paramName + "=" + paramValue);
					}
				}
			}
		}
		return params.toArray(new String[params.size()]);
	}

	protected void displayResults(Map<String, String> data, boolean briefInfo) {
		if (isNewDataDifferentThatPrevious(data)) {
			// Job info data changed since last time.
			StringBuilder displayResults = new StringBuilder("JOB INFO\n");
			for (Entry<String, String> entry : data.entrySet()) {
				String name = entry.getKey();
				if (briefInfo
						&& ("_job_id".equals(name)
								|| "job_active_step".equals(name)
								|| "job_custom_params".equals(name)
								|| "job_start_time".equals(name)
								|| "job_status".equals(name)
								|| "job_workflow_name".equals(name)
								|| "job_workflow_revision".equals(name)
							)
					) {
					continue;
				}
				displayResults.append(name).append(" = ").append(entry.getValue()).append("\n");
			}
			LOGGER.info(displayResults.toString());
			previousDisplayValues = data;
		} else {
			// Job info data did not change since last time.
			LOGGER.info("JOB INFO didn't change.");
		}
	}

	private byte[] buildJobDescriptorMessage() {
		JobDescriptor.Builder jobDescriptorBuilder = JobDescriptor.newBuilder().setWorkflow(workflowName);

		// Add service parameters.
		if (serviceParams != null) {
			for (String param : serviceParams) {
				jobDescriptorBuilder.addConfig(UnicornUtils.prepareServiceConfig(param));
			}
		}

		return jobDescriptorBuilder.build().toByteArray();
	}

	/**
	 * Checks if new job info data is different than previous one.
	 * 
	 * @param newData
	 * @return True if new data is different than old. False if both are the same.
	 */
	private boolean isNewDataDifferentThatPrevious(Map<String, String> newData) {
		boolean result = false;
		if (previousDisplayValues == null || newData.size() != previousDisplayValues.size()) {
			result = true;
		} else {
			Set<Entry<String, String>> esNew = newData.entrySet();
			Set<Entry<String, String>> esOld = previousDisplayValues.entrySet();
			Iterator<Entry<String, String>> itOld = esOld.iterator();
			Iterator<Entry<String, String>> itNew = esNew.iterator();

			while (itOld.hasNext()) {
				Entry<String, String> entryOld = itOld.next();
				Entry<String, String> entryNew = itNew.next();
				if ("job_processing_time_sec".equals(entryNew.getKey())) {
					continue;
				}
				if (!entryNew.getKey().equals(entryOld.getKey()) || !entryNew.getValue().equals(entryOld.getValue())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	public static class Builder extends AbstractCommandBuilder {

		@Override
		protected Command buildCommand(CommandLineParams cmdParams,
				CommandLine cmd) throws ConnectionException {
				// Repeat job given number of times.

				// Check for arguments number.
				String[] options = cmd.getOptionValues("jdl");
				if (options.length != 3 && options.length != 4) {
					throw new IllegalStateException("Wrong number of arguments for -jdl option. " + Arrays.toString(options));
				}

				// Parse arguments.
				String actionType = options[0];
				long loopCount = 1;

				// Parse 'count' argument.
				try {
					loopCount = Long.valueOf(options[2]);
					if (loopCount < 1) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					throw new IllegalStateException("Third argument for -jdl option should be positive number. Provided: " + options[2]);
				}

				// Parse 'interval' argument.
				Long sleepTime = null;
				if (options.length == 4) {
					try {
						sleepTime = Long.valueOf(options[3]);
						if (sleepTime < 1) {
							throw new NumberFormatException();
						}
					} catch (NumberFormatException e) {
						throw new IllegalStateException("Third argument for -jdl option should be positive number. Provided: " + options[2]);
					}
				}

				// 'action' argument should be 'id' or 'w' only.
				if ("id".equals(actionType)) {
					try {
						// 1st argument is 'id', 2nd argument has to be positive number.
						long jobId = Long.parseLong(options[1]);
						if (jobId < 1) {
							throw new NumberFormatException();
						}
						return new JobDescriptorLoopedCommand(cmdParams.getFrameworkQueueName(), jobId, loopCount, sleepTime);
					} catch (NumberFormatException e) {
						throw new IllegalStateException("'param' argument for -jdl option should be positive number. Provided: " + options[1]);
					}
				} else if ("w".equals(actionType)) {
					// 'action' argument is 'w'.
					return new JobDescriptorLoopedCommand(cmdParams.getFrameworkQueueName(), options[1], loopCount, sleepTime);
				} else {
					// Wrong 'action' argument.
					throw new IllegalStateException("'action' argument for -jdl option should be 'id' or 'w'. Provided: " + actionType);
				}
		}
	}
}
