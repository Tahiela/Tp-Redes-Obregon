package Models;

public class Dispositivo {
    private String ip;
    private String nombre;
    private boolean activo;
    private long tiempoRespuestaMs;

    public Dispositivo(String ip, String nombre, boolean activo, long tiempoRespuestaMs) {
        this.ip = ip;
        this.nombre = nombre;
        this.activo = activo;
        this.tiempoRespuestaMs = tiempoRespuestaMs;
    }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public long getTiempoRespuestaMs() { return tiempoRespuestaMs; }
    public void setTiempoRespuestaMs(long tiempoRespuestaMs) { this.tiempoRespuestaMs = tiempoRespuestaMs; }
}