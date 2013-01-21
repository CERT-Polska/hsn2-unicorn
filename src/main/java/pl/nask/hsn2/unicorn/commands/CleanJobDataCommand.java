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

import pl.nask.hsn2.protobuff.Jobs.JobFinished;
import pl.nask.hsn2.protobuff.Jobs.JobStatus;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public class CleanJobDataCommand extends BasicRPCCommand {

	private final static String REQUEST_TYPE = "JobFinished";
	private Long jobId;

	public CleanJobDataCommand(String queueName, Long jobId) throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.jobId = jobId;
	}

	@Override
	protected void buildMessage() {
		JobFinished jobFinished = JobFinished.newBuilder().setJob(jobId).setStatus(JobStatus.COMPLETED).build();
		message = jobFinished.toByteArray();
	}

	@Override
	protected void executeSpecyficJob() throws ConnectionException {
		connector.send(request);
		LOGGER.info("JobFinished message sent. Job id = {}.", jobId);
	}
}
