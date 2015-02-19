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

package pl.nask.hsn2.unicorn.commands.queue;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.commands.ListenCommand;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Response;

// Get and print messages from queue.
public class StreamMessagesCommand extends ListenCommand {

	public StreamMessagesCommand(String queueName) throws ConnectionException {
		super(queueName);
		connector.connectAutoAckListener(queueName);
	}

	public void execute() throws ConnectionException {
		LOGGER.info("Waiting for messages...");
		while (true) {
			Response response = connector.receive();
			LOGGER.info(response.toString());
		}
	}
	
	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,
				CommandLine cmd) throws ConnectionException {
			return new StreamMessagesCommand(cmd.getOptionValue("sm"));
		}
	}
}
