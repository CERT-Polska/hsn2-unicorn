package pl.nask.hsn2.unicorn.commands;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public abstract class AbstractCommandBuilder implements CommandBuilder {

	@Override
	public final Command build(CommandLineParams cmdParams) {
		try {
			return buildCommand(cmdParams, cmdParams.getCmd());
		} catch (ConnectionException e) {
			throw new IllegalStateException(e);
		}
	}

	protected abstract Command buildCommand(CommandLineParams cmdParams, CommandLine cmd) throws ConnectionException;

}
