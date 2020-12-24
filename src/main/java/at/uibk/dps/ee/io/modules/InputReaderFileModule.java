package at.uibk.dps.ee.io.modules;

import org.opt4j.core.config.annotations.File;
import org.opt4j.core.config.annotations.Info;
import org.opt4j.core.config.annotations.Order;
import org.opt4j.core.start.Constant;

import at.uibk.dps.ee.core.InputDataProvider;
import at.uibk.dps.ee.guice.modules.InputModule;
import at.uibk.dps.ee.io.input.InputDataProviderFile;

/**
 * Module to read the Json Object used as WF input from a file.
 * 
 * @author Fedor Smirnov
 *
 */
public class InputReaderFileModule extends InputModule {

	@Order(1)
	@Info("Filepath to the .json file containing the WF input.")
	@Constant(value = "filePath", namespace = InputDataProviderFile.class)
	@File
	public String filePath = "";

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	protected void config() {
		bind(InputDataProvider.class).to(InputDataProviderFile.class);
	}
}
