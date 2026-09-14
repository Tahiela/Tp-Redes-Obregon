package Views;

import Models.Dispositivo;
import Services.ScannerRedLogic;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class InterfazEscaner extends JFrame {

    private JTextField txtIpInicio, txtIpFin, txtTimeout, txtReintentos;
    private JTable tablaResultados;
    private DefaultTableModel modelTabla;
    private JProgressBar progressBar;
    private JLabel lblEquiposActivos;
    private JButton btnIniciar, btnDetener, btnLimpiar, btnGuardar, btnFiltrar;

    private SwingWorker<Void, Dispositivo> worker;
    private List<Dispositivo> listaEscaneados = new ArrayList<>();
    private boolean soloActivos = false;

    public InterfazEscaner() {
        setTitle("Escáner de Red");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelConfig = new JPanel(new GridLayout(4, 2, 5, 5));
        panelConfig.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelConfig.add(new JLabel("IP de inicio:"));
        txtIpInicio = new JTextField("10.160.7.223");
        panelConfig.add(txtIpInicio);

        panelConfig.add(new JLabel("IP de fin:"));
        txtIpFin = new JTextField("10.160.7.233");
        panelConfig.add(txtIpFin);

        panelConfig.add(new JLabel("Tiempo de espera (ms):"));
        txtTimeout = new JTextField("1000");
        panelConfig.add(txtTimeout);

        panelConfig.add(new JLabel("Número de reintentos:"));
        txtReintentos = new JTextField("1");
        panelConfig.add(txtReintentos);

        add(panelConfig, BorderLayout.NORTH);

        String[] columnas = {"IP", "Nombre equipo", "Activo", "Tiempo (ms)"};
        modelTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Boolean.class;
                return String.class;
            }
        };
        tablaResultados = new JTable(modelTabla);
        tablaResultados.setAutoCreateRowSorter(true);
        add(new JScrollPane(tablaResultados), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Listo");
        panelInferior.add(progressBar, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout());

        lblEquiposActivos = new JLabel("Equipos activos: 0");

        btnIniciar = new JButton("Iniciar escaneo");
        btnDetener = new JButton("Detener escaneo");
        btnLimpiar = new JButton("Limpiar");
        btnGuardar = new JButton("Guardar resultados");
        btnFiltrar = new JButton("Mostrar solo activos");

        btnDetener.setEnabled(false);

        panelBotones.add(lblEquiposActivos);
        panelBotones.add(btnIniciar);
        panelBotones.add(btnDetener);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnFiltrar);

        panelInferior.add(panelBotones, BorderLayout.SOUTH);
        add(panelInferior, BorderLayout.SOUTH);

        btnIniciar.addActionListener(e -> iniciarEscaneo());
        btnDetener.addActionListener(e -> detenerEscaneo());
        btnLimpiar.addActionListener(e -> limpiarTabla());
        btnGuardar.addActionListener(e -> guardarResultados());
        btnFiltrar.addActionListener(e -> aplicarFiltro());
    }

    private void iniciarEscaneo() {
        String ipInicio = txtIpInicio.getText().trim();
        String ipFin = txtIpFin.getText().trim();

        if (!ScannerRedLogic.esIPValida(ipInicio) || !ScannerRedLogic.esIPValida(ipFin)) {
            JOptionPane.showMessageDialog(this, "Formato de IP inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        long startLong = ScannerRedLogic.ipToLong(ipInicio);
        long endLong = ScannerRedLogic.ipToLong(ipFin);

        if (startLong > endLong) {
            JOptionPane.showMessageDialog(this, "La IP de inicio debe ser menor o igual a la IP de fin.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int timeout = Integer.parseInt(txtTimeout.getText().trim());

        modelTabla.setRowCount(0);
        listaEscaneados.clear();

        btnIniciar.setEnabled(false);
        btnDetener.setEnabled(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum((int) (endLong - startLong + 1));
        progressBar.setValue(0);
        progressBar.setString("Escaneando...");

        worker = new SwingWorker<>() {
            private int activosCount = 0;

            @Override
            protected Void doInBackground() {
                int progreso = 0;
                for (long ip = startLong; ip <= endLong && !isCancelled(); ip++) {
                    String ipActual = ScannerRedLogic.longToIp(ip);
                    Dispositivo d = ScannerRedLogic.escanearIP(ipActual, timeout);

                    if (d.isActivo()) activosCount++;
                    publish(d);

                    progreso++;
                    setProgress(progreso);
                }
                return null;
            }

            @Override
            protected void process(List<Dispositivo> chunks) {
                for (Dispositivo d : chunks) {
                    listaEscaneados.add(d);
                    if (!soloActivos || d.isActivo()) {
                        modelTabla.addRow(new Object[]{d.getIp(), d.getNombre(), d.isActivo(), d.getTiempoRespuestaMs()});
                    }
                }
                lblEquiposActivos.setText("Equipos activos: " + activosCount);
                progressBar.setValue(listaEscaneados.size());
            }

            @Override
            protected void done() {
                btnIniciar.setEnabled(true);
                btnDetener.setEnabled(false);
                if (isCancelled()) {
                    progressBar.setString("Escaneo cancelado");
                } else {
                    progressBar.setString("Escaneo finalizado");
                }
            }
        };

        worker.execute();
    }

    private void detenerEscaneo() {
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
    }

    private void limpiarTabla() {
        modelTabla.setRowCount(0);
        listaEscaneados.clear();
        lblEquiposActivos.setText("Equipos activos: 0");
        progressBar.setValue(0);
        progressBar.setString("Listo");
    }

    private void aplicarFiltro() {
        soloActivos = !soloActivos;
        btnFiltrar.setText(soloActivos ? "Mostrar todos" : "Mostrar solo activos");
        modelTabla.setRowCount(0);

        for (Dispositivo d : listaEscaneados) {
            if (!soloActivos || d.isActivo()) {
                modelTabla.addRow(new Object[]{d.getIp(), d.getNombre(), d.isActivo(), d.getTiempoRespuestaMs()});
            }
        }
    }

    private void guardarResultados() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("escaneo_resultados.txt"))) {
            writer.println("IP,Nombre,Activo,Latencia(ms)");
            for (Dispositivo d : listaEscaneados) {
                writer.printf("%s,%s,%b,%d\n", d.getIp(), d.getNombre(), d.isActivo(), d.getTiempoRespuestaMs());
            }
            JOptionPane.showMessageDialog(this, "Resultados guardados en escaneo_resultados.txt");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}