/**
 * 
 */
package org.snova.framework.config;

import org.arch.config.IniProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.framework.common.Constants;
import org.snova.framework.util.ReloadableFileMonitor;
import org.snova.framework.util.ReloadableFileMonitorManager;

import java.io.*;

/**
 * @author wqy
 * 
 */
public class SnovaConfiguration implements ReloadableFileMonitor
{
	protected static Logger logger = LoggerFactory
	        .getLogger(SnovaConfiguration.class);

	private static SnovaConfiguration instance = new SnovaConfiguration();

	private IniProperties props = new IniProperties();

	private static String home = null;

//	public static String getHome()
//	{
//		getInstance();
//		home = System.getProperty(Constants.APP_HOME);
//		if (null == home)
//		{
//			home = ".";
//		}
//        System.out.println("App home:"+home);
//		return home;
//	}

	private SnovaConfiguration()
	{
		loadConfig();
		ReloadableFileMonitorManager.getInstance().registerConfigFile(this);
		home = System.getProperty(Constants.APP_HOME);
	}

	public static SnovaConfiguration getInstance()
	{
		return instance;
	}

	public IniProperties getIniProperties()
	{
		return props;
	}

	private static File getConfigFile()
	{
        File confile=new File("conf/"+Constants.CONF_FILE);
//        System.out.println("confile:"+confile.getAbsolutePath());

		return confile;
	}

	private void loadConfig()
	{
        InputStream is = null;
        try {
            is = new FileInputStream(getConfigFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("Cannot load config file:"+getConfigFile().getAbsolutePath());
        }
        props = new IniProperties();
		if (null != is)
		{
			try
			{
				props.load(is);
			}
			catch (Exception e)
			{
				logger.error("Failed to load config file:"
				        + Constants.CONF_FILE, e);
			}
		}
	}

	public void save()
	{
		File confFile = getConfigFile();
		try
		{
			FileOutputStream fos = new FileOutputStream(confFile);
			props.store(fos);
		}
		catch (Exception e)
		{
			logger.error("Failed to save config file:" + confFile.getName());
		}
	}

	@Override
	public void reload()
	{
		loadConfig();
	}

	@Override
	public File getMonitorFile()
	{
		return getConfigFile();
	}

	public void setProxyService(String handlerName)
	{
		props.setProperty("SPAC", "Default", handlerName);

	}
}
