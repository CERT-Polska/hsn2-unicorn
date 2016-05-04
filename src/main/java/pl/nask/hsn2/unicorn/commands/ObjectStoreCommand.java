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

package pl.nask.hsn2.unicorn.commands;

import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.QueryStructure;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.RequestType;
import pl.nask.hsn2.unicorn.FailedCommandException;
import pl.nask.hsn2.unicorn.commands.objectstore.GetObjectCommand;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.OSResponse;
import pl.nask.hsn2.unicorn.connector.Response;

public abstract class ObjectStoreCommand extends RPCCommand {

	private static final String REGEX_HELPER_1 = "\\}";
	private static final String DIVIDER_REPLACE = "\n==================================\ndata \\{";
	private static final String DIVIDER_SEARCH = "data \\{";
	private static final String REQUEST_TYPE = "ObjectRequest";
	private static final String DIVIDER_NEW_LINE_FINAL = ";NLF:";
	private static final String DIVIDER_NEW_LINE = ";NL:";
	protected Long jobId;
	protected OSResponse osResponse;
	protected boolean verbose;
	private boolean brief;

	public ObjectStoreCommand(String queueName, Long jobId) throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.jobId = jobId;
	}

	protected String osResponseToString() {
		if (brief) {
			String result = osResponse.toString().trim();
			// Remove message header.
			result = result.replaceAll("type: SUCCESS_GET", DIVIDER_NEW_LINE_FINAL + DIVIDER_NEW_LINE_FINAL);

			// Remove new line characters to keep everything as a single line.
			result = result.replaceAll("\\r", "");
			result = result.replaceAll("\\n\\s*", DIVIDER_NEW_LINE);

			// Change simple attribute.
			result = result.replaceAll(DIVIDER_NEW_LINE + "attrs \\{" + DIVIDER_NEW_LINE + "name: \\\"([a-z0-9_]+?)\\\"" + DIVIDER_NEW_LINE + "type: [A-Z]+?"
					+ DIVIDER_NEW_LINE + "\\w+: \\\"?(.+?)\\\"?" + DIVIDER_NEW_LINE + REGEX_HELPER_1, "$1=$2" + DIVIDER_NEW_LINE_FINAL);

			// Change nested attribute.
			result = result.replaceAll(DIVIDER_NEW_LINE + "attrs \\{" + DIVIDER_NEW_LINE + "name: \\\"([a-z0-9_]+?)\\\"" + DIVIDER_NEW_LINE + "type: [A-Z]+?"
					+ DIVIDER_NEW_LINE + "\\w+? \\{" + DIVIDER_NEW_LINE + "(.+?)" + DIVIDER_NEW_LINE + "(.+?)" + DIVIDER_NEW_LINE + REGEX_HELPER_1 + DIVIDER_NEW_LINE
					+ REGEX_HELPER_1, "$1=$2,$3" + DIVIDER_NEW_LINE_FINAL);

			// Change id.
			result = result.replaceAll(DIVIDER_NEW_LINE + "id: (\\d+?)", "id=$1" + DIVIDER_NEW_LINE_FINAL);

			// Change object header.
			result = result.replaceAll(DIVIDER_NEW_LINE + "data \\{(.+?)" + DIVIDER_NEW_LINE + REGEX_HELPER_1, "---[Object]-----------------------------"
					+ DIVIDER_NEW_LINE_FINAL + "$1" + DIVIDER_NEW_LINE_FINAL);

			// Change objects counter at the end.
			result = result.replaceAll(DIVIDER_NEW_LINE + "count: (\\d+?)", DIVIDER_NEW_LINE_FINAL + "object counter=$1");

			// Make new lines again.
			result = result.replaceAll(DIVIDER_NEW_LINE_FINAL, "\n");

			return result;
		} else {
			return osResponse.toString().replaceAll(DIVIDER_SEARCH, DIVIDER_REPLACE);
		}
	}

	protected void sendRequestAndReceiveOSResponse() throws ConnectionException, FailedCommandException {
		connector.send(request);
		Response response = connector.receive();
		this.osResponse = response.getOSResponse();
		if (verbose) {
			try {
				getObjectsDetails();
			} catch (FailedCommandException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}

	protected final ObjectRequest buildQueryMessage(QueryStructure query) {
		return ObjectRequest.newBuilder().setJob(jobId).setType(RequestType.QUERY).addQuery(query).build();
	}

	protected final ObjectRequest buildQueryMessage() {
		return ObjectRequest.newBuilder().setJob(jobId).setType(RequestType.QUERY).build();
	}

	protected void getObjectsDetails() throws FailedCommandException, ConnectionException {
		GetObjectCommand command = new GetObjectCommand(destinationQueueName, jobId, osResponse.getObjects());
		command.execute();
		osResponse = command.osResponse;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setBrief(boolean brief) {
		this.brief = brief;
	}
}
