package services;

import model.Dispositivo;
import java.net.InetAddress;

public class ScannerRedLogic {
	    public Dispositivo escanearIP(String ipStr, int timeoutMs, int reintentos) {
	        boolean activo = false;
	        long tiempoRespuesta = 0;
	        String nombreEquipo = "Desconocido";

	        try {
	            InetAddress address = InetAddress.getByName(ipStr);
	            long tiempoInicio = System.currentTimeMillis();

	            for (int i = 0; i < reintentos; i++) {
	                if (address.isReachable(timeoutMs)) {
	                    activo = true;
	                    tiempoRespuesta = System.currentTimeMillis() - tiempoInicio;
	                    break;
	                }
	            }

	            if (activo) {
	                String host = address.getCanonicalHostName();
	                if (!host.equals(ipStr)) {
	                    nombreEquipo = host;
	                }
	            }

	        } catch (Exception e) {
	            activo = false;
	        }

	        return new Dispositivo(ipStr, nombreEquipo, activo, tiempoRespuesta);
	    }
	}