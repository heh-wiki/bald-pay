package wiki.heh.bald.pay.common.util;

import java.util.Date;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *  属性文件工具类,支持缓存及刷新
 * @author heh
 * @date 2020-07-05
 * @version v1.0

 */
public class MyProperties {

	private static HashMap<String, MyProperties> configMap = new HashMap<String, MyProperties>();

	private Date loadTime = null;
	private ResourceBundle rb = null;
	private static final String CONFIG_FILE = "properties";
	private static final long CONFIG_CACHE_TIME = 60 * 1000; // 缓存1分钟

	private MyProperties(String name) {
		this.loadTime = new Date();
		this.rb = ResourceBundle.getBundle(name);
	}

	public static synchronized MyProperties getInstance(String name) {
		MyProperties conf = configMap.get(name);
		if (null == conf) {
			conf = new MyProperties(name);
			configMap.put(name, conf);
			return conf;
		}
		if (new Date().getTime() - conf.getLoadTime().getTime() > CONFIG_CACHE_TIME) {
			conf = new MyProperties(name);
			configMap.put(name, conf);
			return conf;
		}
		return conf;
	}

	public static synchronized MyProperties getInstance() {
		return getInstance("config");
	}

	public Date getLoadTime() {
		return loadTime;
	}

	public String getValue(String key) {
		try {
			String v = rb.getString(key);
			return v;
		}catch (MissingResourceException e) {
			return "";
		}
	}

	public boolean getBool(String key) {
		String v = getValue(key);
		if (v.equalsIgnoreCase("true"))
			return true;
		return false;
	}

	public int getInt(String key) {
		String v = getValue(key);
		return Integer.parseInt(v);
	}

	public long getLong(String key) {
		String v = getValue(key);
		return Long.parseLong(v);
	}

	public static void main(String[] args) throws InterruptedException {
		for (int i=0; i<10; i++) {
			String v = MyProperties.getInstance("common").getValue("jdbc.jndi.name");
			Thread.sleep(1000 * 30);
		}

	}

}
