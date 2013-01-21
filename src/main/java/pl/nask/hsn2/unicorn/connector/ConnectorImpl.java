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

package pl.nask.hsn2.unicorn.connector;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

public class ConnectorImpl implements Connector {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorImpl.class);
	private static volatile ConnectorImpl connector;

	private String replyQueueName;
	private String address;

	private Connection connection;
	private Channel channel;
	private QueueingConsumer consumer;

	private ConnectorImpl(){}

	public static Connector getInstance(){
		if (connector == null)
			connector = new ConnectorImpl();
		return connector;
	}

	public void setServerAddress(String address) {
		this.address = address;
	}

	public void connectRPC() throws ConnectionException {
		if (connection == null || !connection.isOpen()){
			createConnection();
			try {
				replyQueueName = channel.queueDeclare().getQueue();
				prepereConsumerQueue(true);
			} catch (IOException e) {
				throw new ConnectionException("Creating reply queue error!", e);
			}

		}
	}

	public void connectAutoAckListener(String replyQueueName) throws ConnectionException{
		connectListener(replyQueueName, true);
	}

	public void connectManualAckListener(String replyQueueName) throws ConnectionException{
		connectListener(replyQueueName, false);
	}

	private void connectListener(String replyQueueName, boolean autoAck) throws ConnectionException {
		if (connection == null || !connection.isOpen()){
			createConnection();
	        this.replyQueueName = replyQueueName;
	        prepereConsumerQueue(autoAck);
		}
	}

	private void createConnection() throws ConnectionException{
		ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(address);

        try {
			connection = factory.newConnection();
			channel = connection.createChannel();
		} catch (IOException e) {
			throw new ConnectionException("Creating connection error!", e);
		}
	}

	private void prepereConsumerQueue(boolean autoAck) throws ConnectionException {
		consumer = new QueueingConsumer(channel);
		try {
			channel.basicConsume(replyQueueName, autoAck, consumer);
		} catch (IOException e) {
			throw new ConnectionException("Creating consumer error!", e);
		}
	}

	public void send(Request request) throws ConnectionException {
		request.setReplyQueueName(replyQueueName);
		try {
			channel.basicPublish("", request.getDestination(), request.getProperties(), request.getMessage());
		} catch (IOException e) {
			throw new ConnectionException("Sending error!", e);
		}
	}

	public Response receive() throws ConnectionException{
		try {
			Delivery delivery = consumer.nextDelivery();
	        BasicProperties properties = delivery.getProperties();
	        return new Response(properties.getType(), properties.getContentType(), delivery.getBody());
		} catch (ShutdownSignalException e) {
			throw new ConnectionException("Receiving error!", e);
		} catch (InterruptedException e) {
			throw new ConnectionException("Receiving error!", e);
		}
	}

	public void close() {
		if (connection != null){
	    	try {
				connection.close();
			} catch (IOException e) {
				LOGGER.error("Can't close connection.",e);
			}
		}
	}

}
