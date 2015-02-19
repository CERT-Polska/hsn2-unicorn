package pl.nask.hsn2.unicorn.commands;

import pl.nask.hsn2.unicorn.CommandLineParams;

public interface CommandBuilder {

	Command build(CommandLineParams cmdParams);

}
