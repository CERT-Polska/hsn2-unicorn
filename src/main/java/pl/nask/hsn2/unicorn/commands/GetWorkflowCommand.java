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

import pl.nask.hsn2.protobuff.Workflows.WorkflowGetRequest;
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

}
