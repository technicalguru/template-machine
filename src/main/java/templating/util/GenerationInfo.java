package templating.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides statistical information about the generation process.
 * @author ralph
 *
 */
public class GenerationInfo {

	private Set<String> languages;
	private int files;
	
	/**
	 * Constructor.
	 */
	public GenerationInfo() {
		this(null, 0);
	}

	/**
	 * Constructor.
	 * @param languages language to set
	 * @param files     files count to set
	 */
	public GenerationInfo(Set<String> languages, int files) {
		this.languages = languages != null ? languages : new HashSet<>();
		this.files = files;
	}

	/**
	 * Add information from other information
	 * @param other the other info
	 */
	public void add(GenerationInfo other) {
		this.languages.addAll(other.getLanguages());
		this.files     += other.getFiles();
	}
	
	/**
	 * Returns the language count.
	 * @return the language count
	 */
	public int getLanguageCount() {
		return languages.size();
	}

	/**
	 * Returns the languages.
	 * @return the languages
	 */
	public Set<String> getLanguages() {
		return languages;
	}

	/**
	 * Sets the languages.
	 * @param languages - the languages to set
	 */
	public void setLanguages(Set<String> languages) {
		this.languages = languages;
	}

	/**
	 * Adds the languages.
	 * @param languages - the languages to set
	 */
	public void addLanguages(Collection<String> languages) {
		this.languages.addAll(languages);
	}

	/**
	 * Returns the files.
	 * @return the files
	 */
	public int getFiles() {
		return files;
	}

	/**
	 * Sets the files.
	 * @param files - the files to set
	 */
	public void setFiles(int files) {
		this.files = files;
	}

	/**
	 * Increased the file counter.
	 */
	public void incFiles() {
		this.files++;
	}
}
