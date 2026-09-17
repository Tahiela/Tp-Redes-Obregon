package model;

public class Dispositivo {
    private String ip;
    private String nombreEquipo;
    private boolean activo;
    private long tiempoRespuesta;

    public Dispositivo(String ip, String nombreEquipo, boolean activo, long tiempoRespuesta) {
        this.ip = ip;
        this.nombreEquipo = nombreEquipo;
        this.activo = activo;
        this.tiempoRespuesta = tiempoRespuesta;
    }

    public String getIp() { return ip; }
    public String getNombreEquipo() { return nombreEquipo; }
    public boolean isActivo() { return activo; }
    public long getTiempoRespuesta() { return tiempoRespuesta; }

    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setTiempoRespuesta(long tiempoRespuesta) { this.tiempoRespuesta = tiempoRespuesta; }
}
