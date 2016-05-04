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

package pl.nask.hsn2.unicorn.commands.objectstore;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.FailedCommandException;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.commands.ObjectStoreCommand;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

// Ask OS for all objects with specific job id.
public class QueryAllCommand extends ObjectStoreCommand {
	public QueryAllCommand(String queueName, Long jobId) throws ConnectionException {
		super(queueName, jobId);
	}

	@Override
	protected void buildMessage() {
		ObjectRequest objectRequest = buildQueryMessage();
		message = objectRequest.toByteArray();
	}

	@Override
	protected void executeSpecyficJob() throws ConnectionException, FailedCommandException {
		sendRequestAndReceiveOSResponse();
		LOGGER.info(osResponseToString());
	}

	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,
				CommandLine cmd) throws ConnectionException {
			Long jobId = Long.valueOf(cmd.getOptionValue("osqa"));
			ObjectStoreCommand command = new QueryAllCommand(cmdParams.getOsQueueName(), jobId);
			command.setVerbose(cmdParams.isVerbose());
			command.setBrief(cmdParams.isBrief());
			return command;
		}
	}
}
