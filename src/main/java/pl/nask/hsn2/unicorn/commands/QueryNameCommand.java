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

import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.QueryStructure;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.QueryStructure.QueryType;
import pl.nask.hsn2.unicorn.FailedCommandException;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public class QueryNameCommand extends ObjectStoreCommand {

	private String attributeName;

	public QueryNameCommand(String queueName, Long jobId, String attributrName) throws ConnectionException {
		super(queueName, jobId);
		this.attributeName = attributrName;
	}

	@Override
	protected void buildMessage() {
		ObjectRequest objectRequest = buildQueryMessage(QueryStructure.newBuilder()
				.setType(QueryType.BY_ATTR_NAME)
				.setAttrName(attributeName)
				.build());
		message = objectRequest.toByteArray();
	}

	@Override
	protected void executeSpecyficJob() throws ConnectionException, FailedCommandException {
		sendRequestAndReceiveOSResponse();
		LOGGER.info(osResponseToString());
	}

}
