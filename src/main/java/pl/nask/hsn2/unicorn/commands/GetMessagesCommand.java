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

import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Response;

public class GetMessagesCommand extends ListenCommand {
	private final static String DIVIDER = "\n=======================================";
	private final int msgNumber;

	public GetMessagesCommand(String queueName, int messagesNumber) throws ConnectionException {
		super(queueName);
		if (messagesNumber < 1) {
			msgNumber = 1;
			LOGGER.info("Illegal messages number. Set to 1.");
		} else {
			msgNumber = messagesNumber;
		}
		connector.connectManualAckListener(queueName);
	}

	public void execute() throws ConnectionException {
		for (int i = 0; i < msgNumber; i++) {
			Response response = connector.receive();
			LOGGER.info(response.toString() + DIVIDER);
		}
	}

}