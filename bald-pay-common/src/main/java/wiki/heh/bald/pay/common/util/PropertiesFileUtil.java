package wiki.heh.bald.pay.common.util;

import java.util.ResourceBundle;

/**
 *  属性文件工具类
 * @author heh
 * @date 2020-11-05
 * @version v1.0

 */
public class PropertiesFileUtil {

	private ResourceBundle rb = null;

	public PropertiesFileUtil(String bundleFile) {
		rb = ResourceBundle.getBundle(bundleFile);
	}

	public String getValue(String key) {
		return rb.getString(key);
	}

	public static void main(String[] args) {


	}
}
