package wiki.heh.bald.pay.common.util;

/**
 * @author: dingzhiwei
 * @date: 2020/11/1
 *
 */
public class StrUtil {

    public static String toString(Object obj) {
        return obj == null?"":obj.toString();
    }

    public static String toString(Object obj, String nullStr) {
        return obj == null?nullStr:obj.toString();
    }

}
