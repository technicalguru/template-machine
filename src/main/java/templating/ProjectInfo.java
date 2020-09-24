/**
 * 
 */
package templating;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Collects information about the generation.
 * @author ralph
 *
 */
public class ProjectInfo {

	protected Set<String> languages;
	
	/**
	 * Constructor.
	 */
	public ProjectInfo() {
		languages = new HashSet<String>();
	}

	/**
	 * Add information about the generation.
	 * @param generator - the generator
	 */
	public void add(Generator generator) {
		languages.addAll(generator.getLanguages());
	}
	
	/**
	 * Return the languages that were generated.
	 * @return the language key set
	 */
	public Collection<String> getLanguages() {
		return languages;
	}
}
