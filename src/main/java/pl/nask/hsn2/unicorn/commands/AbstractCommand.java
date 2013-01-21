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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.unicorn.connector.Connector;
import pl.nask.hsn2.unicorn.connector.ConnectorImpl;

public abstract class AbstractCommand implements Command {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommand.class);

	protected Connector connector;

	protected AbstractCommand() {
		this.connector = ConnectorImpl.getInstance();
	}

}
