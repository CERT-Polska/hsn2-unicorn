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

package pl.nask.hsn2.unicorn.connector;

import java.util.UUID;

import com.rabbitmq.client.AMQP.BasicProperties;

public class Request {
	private static final String DEFAULT_CONTENT_TYPE = "application/hsn2+protobuf";
	private BasicProperties properties = new BasicProperties();
	private String destination;
	private byte[] message;

	public Request(String destination, String requestType, byte[] message) {
		this.destination = destination;
		this.message = message.clone();
		prepareProperties(requestType);
	}

	private void prepareProperties(String requestType){
		String corrId = UUID.randomUUID().toString();
		properties =  properties.builder()
						.contentType(DEFAULT_CONTENT_TYPE)
						.type(requestType)
						.correlationId(corrId)
						.build();
	}

	public BasicProperties getProperties() {
		return properties;
	}

	public String getDestination() {
		return destination;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setReplyQueueName(String replyQueueName) {
		properties = properties.builder()
						.replyTo(replyQueueName)
						.build();
	}
}
