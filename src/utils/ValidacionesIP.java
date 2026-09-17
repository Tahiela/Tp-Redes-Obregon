package utils;

import java.net.InetAddress;

public class ValidacionesIP {
    private static final String IPV4_REGEX = 
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public static boolean esIPv4Valida(String ip) {
        return ip != null && ip.matches(IPV4_REGEX);
    }

    public static long ipToLong(String ipAddress) throws Exception {
        InetAddress ip = InetAddress.getByName(ipAddress);
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= (octet & 0xFF);
        }
        return result;
    }

    public static String longToIp(long ipLong) {
        return String.format("%d.%d.%d.%d",
                (ipLong >> 24) & 0xFF,
                (ipLong >> 16) & 0xFF,
                (ipLong >> 8) & 0xFF,
                ipLong & 0xFF);
    }
}