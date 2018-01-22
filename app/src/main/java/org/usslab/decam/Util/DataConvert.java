package org.usslab.decam.Util;

/**
 * Created by pip on 2017/1/27.
 */

public class DataConvert {
    final private   static String hexStr   = "0123456789ABCDEF";
    final private static char[] hexArray = hexStr.toCharArray();

    public static String bytesToHex(byte[] bytes,char sp) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = sp;
        }
        return new String(hexChars);
    }
    public static String bytesToMac(byte[] b){
        StringBuilder stringBuilder=new StringBuilder(bytesToHex(b,':'));
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
