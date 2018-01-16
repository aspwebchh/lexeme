package common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Common {
    public static Map<String,String> formData2Dic( String formData ) {
        Map<String,String> result = new HashMap<>();
        final String[] items = formData.split("&");
        Arrays.stream(items).forEach(item ->{
            final String[] keyAndVal = item.split("=");
            if( keyAndVal.length == 2) {
                try{
                    final String key = URLDecoder.decode( keyAndVal[0],"utf8");
                    final String val = URLDecoder.decode( keyAndVal[1],"utf8");
                    result.put(key,val);
                }catch (UnsupportedEncodingException e) {}
            }
        });
        return result;
    }

    public static boolean isNullOrEmpty(String str) {
        if( str == null) {
            return true;
        }
        if( str.trim().length() == 0 ) {
            return true;
        }
        return false;
    }

    public static boolean isInteger( String val ) {
        if( val == null || val.trim().length() == 0 ) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\d+$");
        if( pattern.matcher(val.trim()).find()) {
            return true;
        }
        return false;
    }
}