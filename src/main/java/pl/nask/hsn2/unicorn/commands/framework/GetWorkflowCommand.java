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

import pl.nask.hsn2.protobuff.Workflows.WorkflowGetRequest;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.BasicRPCCommand;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public class GetWorkflowCommand extends BasicRPCCommand {
	private final static String REQUEST_TYPE = "WorkflowGetRequest";
	private String workflowName;
	private String revision;

	public GetWorkflowCommand(String queueName, String workflowName, String revision) throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.workflowName = workflowName;
		this.revision = revision;
	}

	@Override
	protected void buildMessage() {
		message = WorkflowGetRequest.newBuilder()
					.setName(workflowName)
					.setRevision(revision)
					.build().toByteArray();
	}
	
	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,
				CommandLine cmd) throws ConnectionException {
			String[] options = cmd.getOptionValues("gw");
			String workflowName = options[0];
			String revision = options.length > 1 ? options[1] : "";
			return new GetWorkflowCommand(cmdParams.getFrameworkQueueName(), workflowName, revision);
		}
	}
}
