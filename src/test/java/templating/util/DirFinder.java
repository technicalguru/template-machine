package templating.util;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps finding dircetories.
 * @author ralph
 *
 */
public class DirFinder {

	private static Logger log = LoggerFactory.getLogger(DirFinder.class);

	/**
	 * Tries to find the file specified from filesystem or classpath.
	 * @param name - name of file, can be fully qualified
	 * @return URL to the file
	 * @see #find(Class, String)
	 */
	public static URL findDir(String name) {
		return findDir(null, name);
	}
	
	/**
	 * Tries to find the directory specified from filesystem or classpath.
	 * <p>The directory will be searched based on the following procedure:</p>
	 * <ul>
	 * <li>Try to find the directory in current working dir (unless absolute path is given).</li>
	 * <li>Try to fine the directory in package of the class given as argument using the default class loader</li>
	 * <li>Try to find the directory in parent packages of the class given as argument using the default class loader</li>
	 * <li>Try to fine the directory in package of the class given as argument using the context class loader</li>
	 * <li>Try to find the directory in parent packages of the class given as argument using the context class loader</li>
	 * <li>Repeat the procedure by trying to find the directory with a prepended slash.</li>
	 * </ul>
	 * 
	 * @param name - name of file, can be fully qualified
	 * @param clazz class to get the class loader from
	 * @return URL to the file
	 */
	public static URL findDir(Class<?> clazz, String name) {
		URL rc = null;
		if (clazz == null) clazz = DirFinder.class;
		
		// try to find as simple file in file system
		try {
			File f = new File(name);
			if (f.exists() && f.isDirectory() && f.canRead()) {
				rc = f.toURI().toURL();
			}
		} catch (Exception e) {
			if (log.isDebugEnabled()) 
				log.debug("No such local directory: "+name, e);
		}
		
		// Create dirs
		String dirs[] = null;
		if (!name.startsWith("/")) {
			dirs = clazz.getPackage().getName().split("\\.");
		}

		// get it from class' class loader
		if ((rc == null) && (clazz != null)) {
				ClassLoader loader = clazz.getClassLoader();
				rc = findDir(loader, dirs, name);
		}
		
		// Not yet found? Use the threads class loader
		if (rc == null) {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (loader != null) rc = findDir(loader, dirs, name);
		}
		
		if (log.isDebugEnabled()) {
			log.debug(name+" located at "+rc);
		}
		if (rc == null) {
			// Try with prepended slash if possible
			if (!name.startsWith("/") && !name.startsWith(".")) {
				rc = findDir(clazz, "/"+name);
			}
		}
		return rc;
	}
	
	/**
	 * Find the resource using the given class loader.
	 * @param classLoader the loader to be used
	 * @param dirs the directory parts (e.g. from package name)
	 * @param name the name of the resource
	 * @return the URL of the resource if found
	 */
	private static URL findDir(ClassLoader classLoader, String dirs[], String name) {
		URL rc = null;
		if (dirs != null) {
			try {
				if (!name.startsWith("/")) {
					for (int i=dirs.length; i>0; i--) {
						String pkgDir = StringUtils.join(dirs, '/', 0, i);
						rc = classLoader.getResource(pkgDir+"/"+name);
						if (rc != null) break;
					}
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()) 
					log.debug("No such classpath file: "+name, e);
			}
		}
		if (rc == null) {
			rc = classLoader.getResource(name);
		}
		return rc;
	}
	

}
