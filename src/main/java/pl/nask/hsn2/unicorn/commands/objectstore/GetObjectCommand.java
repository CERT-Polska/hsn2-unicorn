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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.RequestType;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.FailedCommandException;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.commands.ObjectStoreCommand;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

// Return object having the same jobId and objectId.
public class GetObjectCommand extends ObjectStoreCommand {
	private List<Long> ids = new ArrayList<Long>();
	private boolean quiet = false;

	public GetObjectCommand(String queueName, Long jobId, Long id) throws ConnectionException {
		super(queueName, jobId);
		ids.add(id);
	}

	public GetObjectCommand(String queueName, Long jobId, List<Long> ids) throws ConnectionException {
		super(queueName, jobId);
		this.ids.addAll(ids);
		this.quiet = true;
	}

	@Override
	protected void buildMessage() {
		ObjectRequest objectRequest = ObjectRequest.newBuilder()
				.setJob(jobId)
				.setType(RequestType.GET)
				.addAllObjects(ids)
				.build();
		message =  objectRequest.toByteArray();
	}

	@Override
	protected void executeSpecyficJob() throws ConnectionException, FailedCommandException {
		sendRequestAndReceiveOSResponse();
		if (!quiet){
			LOGGER.info(osResponseToString());
		}
	}

	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,
				CommandLine cmd) throws ConnectionException {
			String[] options = cmd.getOptionValues("osg");
			Long job = Long.valueOf(options[0]);
			Long id = Long.valueOf(options[1]);
			return new GetObjectCommand(cmdParams.getOsQueueName(), job, id);
		}
	}
}
