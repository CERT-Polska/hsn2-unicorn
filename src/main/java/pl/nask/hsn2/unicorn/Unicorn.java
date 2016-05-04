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

package pl.nask.hsn2.unicorn;

import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Connector;
import pl.nask.hsn2.unicorn.connector.ConnectorImpl;

public final class Unicorn {
	private static Connector connector = null;
	private static CommandLineParams cmdParams;

	private Unicorn() {
	}

	/**
	 * @param args
	 * @throws ConnectionException
	 * @throws FailedCommandException
	 */
	public static void main(String[] args) throws ConnectionException, FailedCommandException {
		try {
			initCmdAndConnect(args);

			Command command = CommandFactory.commandInstanceFor(cmdParams);
			command.execute();
		} finally {
			if (connector != null) {
				connector.close();
			}
		}

	}

	private static void initCmdAndConnect(String[] args) {
		String[] arguments = args.clone();
		cmdParams = new CommandLineParams(arguments);
		connector = ConnectorImpl.getInstance();
		connector.setServerAddress(cmdParams.getAddress());
	}
}
