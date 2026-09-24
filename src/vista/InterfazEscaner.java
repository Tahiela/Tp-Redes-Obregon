package vista;


import model.Dispositivo;
import servicios.ScannerRedLogic;
import utils.ValidacionesIP;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class InterfazEscaner extends JFrame {

    private JTextField txtIpInicio, txtIpFin, txtTimeout, txtReintentos;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private JProgressBar barraProgreso;
    private JLabel lblEstado, lblEquiposActivos;
    private JButton btnIniciar, btnDetener, btnLimpiar, btnGuardar, btnFiltrarActivos;

    private SwingWorker<Void, Dispositivo> workerEscaneo;
    private List<Dispositivo> listaCompleta = new ArrayList<>();
    private boolean mostrandoSoloActivos = false;
    private int contadorActivos = 0;

    // Paleta de colores Dark Mode
    private final Color COLOR_FONDO = new Color(24, 28, 36);
    private final Color COLOR_PANEL = new Color(33, 38, 49);
    private final Color COLOR_TEXTO = new Color(240, 243, 246);
    private final Color COLOR_PRIMARIO = new Color(0, 168, 204);
    private final Color COLOR_VERDE = new Color(46, 204, 113);
    private final Color COLOR_ROJO = new Color(231, 76, 60);

    public InterfazEscaner() {
        setTitle("Network Scanner Pro v2.0");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal con margen
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBackground(COLOR_FONDO);
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(panelPrincipal);

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // --- PANEL SUPERIOR (CONFIGURACIÓN) ---
        JPanel panelConfig = new JPanel(new GridLayout(2, 4, 10, 10));
        panelConfig.setBackground(COLOR_PANEL);
        TitledBorder borderConfig = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARIO, 1), 
                " Configuración del Rango de Red "
        );
        borderConfig.setTitleColor(COLOR_PRIMARIO);
        borderConfig.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        panelConfig.setBorder(BorderFactory.createCompoundBorder(borderConfig, new EmptyBorder(10, 10, 10, 10)));

        txtIpInicio = crearCampoTexto("10.160.7.223");
        txtIpFin = crearCampoTexto("10.160.7.233");
        txtTimeout = crearCampoTexto("1000");
        txtReintentos = crearCampoTexto("1");

        panelConfig.add(crearEtiqueta("IP Inicio:"));
        panelConfig.add(txtIpInicio);
        panelConfig.add(crearEtiqueta("Timeout (ms):"));
        panelConfig.add(txtTimeout);

        panelConfig.add(crearEtiqueta("IP Fin:"));
        panelConfig.add(txtIpFin);
        panelConfig.add(crearEtiqueta("Reintentos:"));
        panelConfig.add(txtReintentos);

        add(panelConfig, BorderLayout.NORTH);

        // --- TABLA DE RESULTADOS ---
        String[] columnas = {"IP Target", "Nombre de Host", "Estado", "Latencia (ms)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaResultados = new JTable(modeloTabla);
        tablaResultados.setBackground(COLOR_PANEL);
        tablaResultados.setForeground(COLOR_TEXTO);
        tablaResultados.setGridColor(new Color(50, 58, 70));
        tablaResultados.setRowHeight(28);
        tablaResultados.setFont(new Font("Consolas", Font.PLAIN, 13));

        // Estilo del encabezado de la tabla
        tablaResultados.getTableHeader().setBackground(new Color(18, 22, 28));
        tablaResultados.getTableHeader().setForeground(COLOR_PRIMARIO);
        tablaResultados.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Alineación al centro de los datos
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablaResultados.getColumnCount(); i++) {
            tablaResultados.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        scrollTabla.getViewport().setBackground(COLOR_PANEL);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(50, 58, 70)));
        add(scrollTabla, BorderLayout.CENTER);

        // --- PANEL INFERIOR (CONTROLES Y ESTADO) ---
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setOpaque(false);

        // Barra de progreso y etiquetas
        JPanel panelEstado = new JPanel(new BorderLayout(5, 5));
        panelEstado.setOpaque(false);

        lblEstado = new JLabel("Estado: Listo para escanear", SwingConstants.LEFT);
        lblEstado.setForeground(COLOR_TEXTO);
        lblEstado.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        lblEquiposActivos = new JLabel("Hosts Activos: 0", SwingConstants.RIGHT);
        lblEquiposActivos.setForeground(COLOR_VERDE);
        lblEquiposActivos.setFont(new Font("Segoe UI", Font.BOLD, 13));

        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        barraProgreso.setBackground(COLOR_PANEL);
        barraProgreso.setForeground(COLOR_PRIMARIO);
        barraProgreso.setPreferredSize(new Dimension(100, 22));

        JPanel panelTextosEstado = new JPanel(new BorderLayout());
        panelTextosEstado.setOpaque(false);
        panelTextosEstado.add(lblEstado, BorderLayout.WEST);
        panelTextosEstado.add(lblEquiposActivos, BorderLayout.EAST);

        panelEstado.add(panelTextosEstado, BorderLayout.NORTH);
        panelEstado.add(barraProgreso, BorderLayout.CENTER);

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelBotones.setOpaque(false);

        btnIniciar = crearBoton("Iniciar Escaneo", COLOR_VERDE);
        btnDetener = crearBoton("Detener", COLOR_ROJO);
        btnLimpiar = crearBoton("Limpiar Tabla", new Color(100, 110, 120));
        btnGuardar = crearBoton("Guardar Reporte", COLOR_PRIMARIO);
        btnFiltrarActivos = crearBoton("Solo Activos", new Color(142, 68, 173));

        btnDetener.setEnabled(false);

        panelBotones.add(btnIniciar);
        panelBotones.add(btnDetener);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnFiltrarActivos);
        panelBotones.add(btnGuardar);

        panelInferior.add(panelEstado, BorderLayout.NORTH);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);

        // Eventos de botones
        btnIniciar.addActionListener(e -> iniciarEscaneo());
        btnDetener.addActionListener(e -> detenerEscaneo());
        btnLimpiar.addActionListener(e -> limpiarInterfaz());
        btnGuardar.addActionListener(e -> guardarResultados());
        btnFiltrarActivos.addActionListener(e -> alternarFiltroActivos());
    }

    // --- MÉTODOS AUXILIARES DE DISEÑO ---
    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(COLOR_TEXTO);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }

    private JTextField crearCampoTexto(String textoDefecto) {
        JTextField txt = new JTextField(textoDefecto);
        txt.setBackground(COLOR_FONDO);
        txt.setForeground(COLOR_TEXTO);
        txt.setCaretColor(COLOR_TEXTO);
        txt.setFont(new Font("Consolas", Font.PLAIN, 12));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 70, 85)),
                new EmptyBorder(4, 6, 4, 6)
        ));
        return txt;
    }

    private JButton crearBoton(String texto, Color colorBase) {
        JButton btn = new JButton(texto);
        btn.setBackground(colorBase);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    // --- LÓGICA DE LA INTERFAZ ---
    private void iniciarEscaneo() {
        String ipInicioStr = txtIpInicio.getText().trim();
        String ipFinStr = txtIpFin.getText().trim();

        if (!ValidacionesIP.esIPv4Valida(ipInicioStr) || !ValidacionesIP.esIPv4Valida(ipFinStr)) {
            JOptionPane.showMessageDialog(this, "Ingrese direcciones IPv4 válidas.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            long ipInicio = ValidacionesIP.ipToLong(ipInicioStr);
            long ipFin = ValidacionesIP.ipToLong(ipFinStr);

            if (ipInicio > ipFin) {
                JOptionPane.showMessageDialog(this, "La IP inicial debe ser menor o igual a la IP final.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int timeout = Integer.parseInt(txtTimeout.getText().trim());
            int reintentos = Integer.parseInt(txtReintentos.getText().trim());

            limpiarInterfaz();
            btnIniciar.setEnabled(false);
            btnDetener.setEnabled(true);

            int totalIPs = (int) (ipFin - ipInicio + 1);
            barraProgreso.setMaximum(totalIPs);
            barraProgreso.setValue(0);

            workerEscaneo = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    ScannerRedLogic scanner = new ScannerRedLogic();
                    int procesadas = 0;

                    for (long ipActual = ipInicio; ipActual <= ipFin && !isCancelled(); ipActual++) {
                        String ipStr = ValidacionesIP.longToIp(ipActual);
                        Dispositivo disp = scanner.escanearIP(ipStr, timeout, reintentos);

                        procesadas++;
                        setProgress(procesadas);
                        publish(disp);
                    }
                    return null;
                }

                @Override
                protected void process(List<Dispositivo> chunks) {
                    for (Dispositivo disp : chunks) {
                        listaCompleta.add(disp);
                        barraProgreso.setValue(barraProgreso.getValue() + 1);

                        if (disp.isActivo()) {
                            contadorActivos++;
                            lblEquiposActivos.setText("Hosts Activos: " + contadorActivos);
                        }

                        if (!mostrandoSoloActivos || disp.isActivo()) {
                            agregarFilaTabla(disp);
                        }
                    }
                }

                @Override
                protected void done() {
                    btnIniciar.setEnabled(true);
                    btnDetener.setEnabled(false);
                    lblEstado.setText(isCancelled() ? "Estado: Escaneo Cancelado" : "Estado: Escaneo Completado");
                }
            };

            lblEstado.setText("Estado: Escaneando red...");
            workerEscaneo.execute();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void detenerEscaneo() {
        if (workerEscaneo != null && !workerEscaneo.isDone()) {
            workerEscaneo.cancel(true);
        }
    }

    private void limpiarInterfaz() {
        modeloTabla.setRowCount(0);
        listaCompleta.clear();
        contadorActivos = 0;
        lblEquiposActivos.setText("Hosts Activos: 0");
        barraProgreso.setValue(0);
        lblEstado.setText("Estado: Listo para escanear");
    }

    private void agregarFilaTabla(Dispositivo disp) {
        modeloTabla.addRow(new Object[]{
                disp.getIp(),
                disp.getNombreEquipo(),
                disp.isActivo() ? "ONLINE" : "OFFLINE",
                disp.isActivo() ? disp.getTiempoRespuesta() + " ms" : "-"
        });
    }

    private void guardarResultados() {
        if (listaCompleta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para guardar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File carpeta = new File("resultados");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        File archivo = new File(carpeta, "reporte_escaneo.csv");

        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
            pw.println("IP,Nombre Equipo,Estado,Latencia (ms)");
            for (Dispositivo d : listaCompleta) {
                pw.printf("%s,%s,%s,%s\n",
                        d.getIp(),
                        d.getNombreEquipo(),
                        d.isActivo() ? "ONLINE" : "OFFLINE",
                        d.isActivo() ? d.getTiempoRespuesta() : "");
            }
            JOptionPane.showMessageDialog(this, "Reporte guardado con éxito en:\n" + archivo.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alternarFiltroActivos() {
        mostrandoSoloActivos = !mostrandoSoloActivos;
        btnFiltrarActivos.setText(mostrandoSoloActivos ? "Mostrar Todos" : "Solo Activos");

        modeloTabla.setRowCount(0);
        for (Dispositivo d : listaCompleta) {
            if (!mostrandoSoloActivos || d.isActivo()) {
                agregarFilaTabla(d);
            }
        }
    }
}