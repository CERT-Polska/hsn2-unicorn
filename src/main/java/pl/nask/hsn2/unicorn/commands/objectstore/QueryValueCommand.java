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

import pl.nask.hsn2.protobuff.Object.Attribute;
import pl.nask.hsn2.protobuff.Object.Attribute.Type;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.QueryStructure;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.QueryStructure.QueryType;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.FailedCommandException;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.commands.ObjectStoreCommand;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

// Ask OS for all objects with specific job id, attribute name and value.
public class QueryValueCommand extends ObjectStoreCommand {
	private String attributeName;
	private Attribute attribute;

	public QueryValueCommand(String queueName, Long jobId, String attributeName, Attribute attribute) throws ConnectionException {
		super(queueName, jobId);
		this.attributeName = attributeName;
		this.attribute = attribute;
	}

	@Override
	protected void buildMessage() {
		ObjectRequest objectRequest = buildQueryMessage(QueryStructure.newBuilder()
				.setType(QueryType.BY_ATTR_VALUE)
				.setAttrName(attributeName)
				.setAttrValue(attribute)
				.build());
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
			String[] options = cmd.getOptionValues("osqv");
			Long jobId = Long.valueOf(options[0]);
			String attributeName = options[1];
			String type = options[2];
			String value = options[3];

			Attribute.Builder attribute = Attribute.newBuilder().setName(attributeName);
			if (type.equals("s")) {
				attribute.setDataString(value);
				attribute.setType(Type.STRING);
			} else if (type.equals("i")) {
				attribute.setDataInt(Integer.valueOf(value));
				attribute.setType(Type.INT);
			} else if (type.equals("b")) {
				attribute.setDataBool(Boolean.valueOf(value));
				attribute.setType(Type.BOOL);
			} else if (type.equals("o")) {
				attribute.setDataObject(Long.valueOf(value));
				attribute.setType(Type.OBJECT);
			} else {
				throw new IllegalStateException("Unknown value type: " + type);
			}
			ObjectStoreCommand command = new QueryValueCommand(cmdParams.getOsQueueName(), jobId, attributeName, attribute.build());
			command.setVerbose(cmdParams.isVerbose());
			return command;
		}
	}

}
