package Services;

import Models.Dispositivo;
import java.net.InetAddress;

public class ScannerRedLogic {

    public static Dispositivo escanearIP(String ipStr, int timeoutMs) {
        long inicio = System.currentTimeMillis();
        boolean activo = false;
        String nombre = "Desconocido";
        long latencia = 0;

        try {
            InetAddress address = InetAddress.getByName(ipStr);
            activo = address.isReachable(timeoutMs);
            latencia = System.currentTimeMillis() - inicio;

            if (activo) {
                String hostName = address.getCanonicalHostName();
                if (!hostName.equals(ipStr)) {
                    nombre = hostName;
                }
            }
        } catch (Exception e) {
            activo = false;
        }

        return new Dispositivo(ipStr, nombre, activo, activo ? latencia : 0);
    }

    public static boolean esIPValida(String ip) {
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(regex);
    }

    public static long ipToLong(String ip) {
        String[] octetos = ip.split("\\.");
        long resultado = 0;
        for (int i = 0; i < 4; i++) {
            resultado |= (long) Integer.parseInt(octetos[i]) << ((3 - i) * 8);
        }
        return resultado;
    }

    public static String longToIp(long ipLong) {
        return String.format("%d.%d.%d.%d",
                (ipLong >> 24) & 0xFF,
                (ipLong >> 16) & 0xFF,
                (ipLong >> 8) & 0xFF,
                ipLong & 0xFF);
    }
}