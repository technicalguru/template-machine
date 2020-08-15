/**
 * 
 */
package templating;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * Describes configuration for a run.
 * @author ralph
 *
 */
public class TemplatingConfig {

	protected File    projectRoot;
	protected File    outRoot;
	protected File    subDir;
	protected Date    generationTime;
	protected Charset readEncoding  = Charset.defaultCharset();
	protected Charset writeEncoding = Charset.defaultCharset();

	/**
	 * Constructor.
	 */
	public TemplatingConfig(File projectRoot, File outRoot, Date generationTime) {
		this.projectRoot    = projectRoot;
		this.outRoot        = outRoot;
		this.generationTime = generationTime;
	}

	/**
	 * Returns the encoding for reading templates and language files.
	 * @return the readEncoding
	 */
	public Charset getReadEncoding() {
		return readEncoding;
	}

	/**
	 * Sets the encoding for reading templates and language files.
	 * @param readEncoding - the readEncoding to set
	 */
	public void setReadEncoding(Charset readEncoding) {
		this.readEncoding = readEncoding;
	}

	/**
	 * Returns the encoding for writing generated files.
	 * @return the writeEncoding
	 */
	public Charset getWriteEncoding() {
		return writeEncoding;
	}

	/**
	 * Sets the encoding for writing generated files.
	 * @param writeEncoding - the writeEncoding to set
	 */
	public void setWriteEncoding(Charset writeEncoding) {
		this.writeEncoding = writeEncoding;
	}

	/**
	 * Returns the root director of the project to be generated.
	 * @return the projectRoot
	 */
	public File getProjectRoot() {
		return projectRoot;
	}

	/**
	 * Sets the root director of the project to be generated.
	 * @param projectRoot - the projectRoot to set
	 */
	public void setProjectRoot(File projectRoot) {
		this.projectRoot = projectRoot;
	}

	/**
	 * Returns the output root directory for this project.
	 * @return the outRoot
	 */
	public File getOutRoot() {
		return outRoot;
	}

	/**
	 * Sets the output root directory for this project.
	 * @param outRoot - the outRoot to set
	 */
	public void setOutRoot(File outRoot) {
		this.outRoot = outRoot;
	}

	/**
	 * Returns the generation time for this run.
	 * @return the generationTime
	 */
	public Date getGenerationTime() {
		return generationTime;
	}

	/**
	 * Sets the generation time for this run.
	 * @param generationTime - the generationTime to set
	 */
	public void setGenerationTime(Date generationTime) {
		this.generationTime = generationTime;
	}

	/**
	 * Returns the sub directory to be generated only (can be {@code null}).
	 * @return the subDir
	 */
	public File getSubDir() {
		return subDir;
	}

	/**
	 * Sets the sub directory to be generated only (can be {@code null}).
	 * @param subDir - the subDir to set
	 */
	public void setSubDir(File subDir) {
		this.subDir = subDir;
	}
	
	
}
