package at.uibk.dps.ee.io.modules;

import org.opt4j.core.config.annotations.File;
import org.opt4j.core.config.annotations.Info;
import org.opt4j.core.config.annotations.Order;
import org.opt4j.core.start.Constant;

import at.uibk.dps.ee.guice.modules.InputModule;
import at.uibk.dps.ee.io.EnactmentGraphProvider;
import at.uibk.dps.ee.io.afcl.AfclReader;

/**
 * The {@link AfclReaderFileModule} is used to read in the WF from a file in
 * afcl format.
 * 
 * @author Fedor Smirnov
 *
 */
public class AfclReaderFileModule extends InputModule {

	@Order(1)
	@Info("The path to the .afcl file.")
	@File
	@Constant(value = "filePath", namespace = AfclReader.class)
	public String filePath = "";

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	protected void config() {
		bind(EnactmentGraphProvider.class).to(AfclReader.class);
	}
}
