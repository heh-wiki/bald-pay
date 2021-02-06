package wiki.heh.bald.pay.common.util;

/**
 * @author heh
 * @version v1.0
 * @date 2020-11-30
 */
public class StrUtil {

    public static String toString(Object obj) {
        return obj == null?"":obj.toString();
    }

    public static String toString(Object obj, String nullStr) {
        return obj == null?nullStr:obj.toString();
    }

}
