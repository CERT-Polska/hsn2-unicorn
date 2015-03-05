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

package pl.nask.hsn2.unicorn.commands.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.Jobs.JobInfo;
import pl.nask.hsn2.protobuff.Jobs.JobListReply;
import pl.nask.hsn2.protobuff.Jobs.JobListRequest;
import pl.nask.hsn2.protobuff.Jobs.JobStatus;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.BasicRPCCommand;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Response;

import com.google.protobuf.InvalidProtocolBufferException;

public class JobListCommand extends BasicRPCCommand {

	private final static String REQUEST_TYPE = "JobListRequest";
	private boolean brief;

	public JobListCommand(String queueName) throws ConnectionException {
		super(REQUEST_TYPE, queueName);
	}

	@Override
	protected void buildMessage() {
		JobListRequest jobListRequest = JobListRequest.newBuilder().build();
		message = jobListRequest.toByteArray();
	}

	@Override
	protected void displayResults(Response response) {
		if (brief) {
			if ("JobListReply".equals(response.getType())) {
				// Get response object.
				JobListReply jobListReply = null;
				try {
					jobListReply = JobListReply.parseFrom(response.getBody());
				} catch (InvalidProtocolBufferException e) {
					// Should never happen.
					LOGGER.error("Invalid protocol buffer", e);
					throw new RuntimeException("Invalid protocol buffer");
				}

				// Parse object.
				Map<JobStatus, Set<Long>> jobs = getJobsData(jobListReply);

				// Display results.
				displayResults(getDisplayMessage(jobs));
			} else {
				throw new RuntimeException("Wrong message type");
			}
		} else {
			super.displayResults(response);
		}
	}

	private Map<JobStatus, Set<Long>> getJobsData(JobListReply jobListReply) {
		Map<JobStatus, Set<Long>> jobs = new HashMap<>();
		for (JobInfo job : jobListReply.getJobsList()) {
			JobStatus jobStatus = job.getStatus();
			long jobId = job.getId();
			if (jobs.containsKey(jobStatus)) {
				jobs.get(jobStatus).add(jobId);
			} else {
				Set<Long> newJobsSet = new TreeSet<>();
				newJobsSet.add(jobId);
				jobs.put(jobStatus, newJobsSet);
			}
		}
		return jobs;
	}

	private String getDisplayMessage(Map<JobStatus, Set<Long>> jobs) {
		StringBuilder sb = new StringBuilder("\n\n");
		for (int i = JobStatus.values().length - 1; i >= 0; i--) {
			JobStatus jobStatus = JobStatus.values()[i];
			appendJobInStatusInfo(sb, jobStatus, jobs.get(jobStatus));
		}
		return sb.toString();
	}

	protected void appendJobInStatusInfo(StringBuilder sb, JobStatus status, Set<Long> jobIds) {
		if (jobIds == null)
			return;
		sb.append("Job status: ").append(status).append("\nJobs counter: ").append(jobIds.size()).append("\n");
		boolean isFirstJob = true;
		for (long id : jobIds) {
			if (isFirstJob) {
				isFirstJob = false;
			} else {
				sb.append(", ");
			}
			sb.append(id);
		}
		sb.append("\n\n");
	}

	public void setBrief(boolean brief) {
		this.brief = brief;
	}

	public static class Builder extends AbstractCommandBuilder {

		@Override
		protected Command buildCommand(CommandLineParams cmdParams, CommandLine cmd)
				throws ConnectionException {
			JobListCommand command = new JobListCommand(cmdParams.getFrameworkQueueName());
			command.setBrief(cmdParams.isBrief());
			return command;
		}
	}
}
