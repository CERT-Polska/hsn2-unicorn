package pl.nask.hsn2.unicorn.commands.framework;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.Jobs.JobCancelReply;
import pl.nask.hsn2.protobuff.Jobs.JobCancelRequest;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.BasicRPCCommand;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Response;

import com.google.protobuf.InvalidProtocolBufferException;

public class JobCancelCommand extends BasicRPCCommand {
	
	private final static String REQUEST_TYPE = "JobCancelRequest";
	private Long jobId;
	
	public JobCancelCommand(String queueName, Long jobId) throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.jobId = jobId;
	}

	@Override
	protected void buildMessage() {
		JobCancelRequest cancelRequest = JobCancelRequest.newBuilder().setJob(jobId).build();
		message = cancelRequest.toByteArray();
	}
	
	@Override
	protected void displayResults(Response response) {
		String type = response.getType();
		StringBuilder displayResults = new StringBuilder("Job Cancel Reply\n");
		if ("JobCancelReply".equals(type)) {
			try {
				JobCancelReply reply = JobCancelReply.parseFrom(response.getBody());
				displayResults.append("cancelled: ").append(reply.getCancelled());
				if (reply.hasReason()){
					displayResults.append("\nReason: ").append(reply.getReason());
				}
			} catch (InvalidProtocolBufferException e) {
				// Should never happen.
				displayResults.append("Could not deserialize response message").append(e);
			}
		} 
		else {
			displayResults.append("WRONG MSG TYPE: ").append(type);
		}
		super.displayResults(displayResults.toString());
	}
	
	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,	CommandLine cmd) throws ConnectionException {
			return new JobCancelCommand(cmdParams.getFrameworkQueueName(), Long.valueOf(cmd.getOptionValue("jc")));			
		}
	}
}
