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

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.Jobs.JobCancelReply;
import pl.nask.hsn2.protobuff.Jobs.JobCancelRequest;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.BasicRPCCommand;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Response;

import com.google.protobuf.InvalidProtocolBufferException;

public class JobCancelCommand extends BasicRPCCommand {
	
	private final static String REQUEST_TYPE = "JobCancelRequest";
	private Long jobId;
	
	public JobCancelCommand(String queueName, Long jobId) throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.jobId = jobId;
	}

	@Override
	protected void buildMessage() {
		JobCancelRequest cancelRequest = JobCancelRequest.newBuilder().setJob(jobId).build();
		message = cancelRequest.toByteArray();
	}
	
	@Override
	protected void displayResults(Response response) {
		String type = response.getType();
		StringBuilder displayResults = new StringBuilder("Job Cancel Reply\n");
		if ("JobCancelReply".equals(type)) {
			try {
				JobCancelReply reply = JobCancelReply.parseFrom(response.getBody());
				displayResults.append("cancelled: ").append(reply.getCancelled());
				if (reply.hasReason()){
					displayResults.append("\nReason: ").append(reply.getReason());
				}
			} catch (InvalidProtocolBufferException e) {
				// Should never happen.
				displayResults.append("Could not deserialize response message").append(e);
			}
		} 
		else {
			displayResults.append("WRONG MSG TYPE: ").append(type);
		}
		super.displayResults(displayResults.toString());
	}
	
	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,	CommandLine cmd) throws ConnectionException {
			return new JobCancelCommand(cmdParams.getFrameworkQueueName(), Long.valueOf(cmd.getOptionValue("jc")));			
		}
	}
}
