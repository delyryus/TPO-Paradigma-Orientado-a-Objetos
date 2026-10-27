package interfaz;

import modelo.Alojamiento;
import modelo.Reserva;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Menu del alojamiento. Permite consultar y modificar sus datos, su capacidad
 * y consultar los huespedes alojados en un periodo.
 */
public class PanelAlojamiento extends JPanel {

    private final VentanaPrincipal ventana;

    public PanelAlojamiento(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setLayout(new BorderLayout());

        Alojamiento alojamiento = (Alojamiento) ventana.getSesion();
        add(UtilidadesInterfaz.titulo("Menu de alojamiento - " + alojamiento.getNombre()), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(datosActuales(alojamiento), BorderLayout.NORTH);

        JPanel botones = new JPanel();
        botones.setLayout(new BoxLayout(botones, BoxLayout.Y_AXIS));
        botones.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        botones.add(botonCentrado("Modificar mis datos", e -> modificarDatos()));
        botones.add(botonCentrado("Modificar capacidad", e -> modificarCapacidad()));
        botones.add(botonCentrado("Consultar huespedes en un periodo", e -> consultarHuespedes()));

        centro.add(botones, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JButton botonCerrarSesion = UtilidadesInterfaz.boton("Cerrar sesion", e -> ventana.cerrarSesion());
        JButton botonSalir = UtilidadesInterfaz.boton("Salir", e -> System.exit(0));
        add(UtilidadesInterfaz.panelBotones(botonCerrarSesion, botonSalir), BorderLayout.SOUTH);
    }

    private JPanel datosActuales(Alojamiento alojamiento) {
        JTextArea area = new JTextArea(alojamiento.descripcion());
        area.setEditable(false);
        area.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Mis datos actuales:"), BorderLayout.NORTH);
        panel.add(area, BorderLayout.CENTER);
        return panel;
    }

    private void modificarDatos() {
        Alojamiento alojamiento = (Alojamiento) ventana.getSesion();

        JTextField campoNombre = new JTextField(alojamiento.getNombre(), 20);
        JTextField campoDireccion = new JTextField(alojamiento.getDireccion(), 20);
        JComboBox<modelo.Ciudad> comboCiudad = new JComboBox<>(modelo.Ciudad.values());
        comboCiudad.setSelectedItem(alojamiento.getCiudad());

        JPanel form = UtilidadesInterfaz.formulario(
                UtilidadesInterfaz.fila("Nombre:", campoNombre),
                UtilidadesInterfaz.fila("Ciudad:", comboCiudad),
                UtilidadesInterfaz.fila("Direccion:", campoDireccion)
        );

        int opcion = JOptionPane.showConfirmDialog(this, form, "Modificar mis datos",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            ventana.getGestorAlojamientos().modificarDatos(
                    alojamiento, campoNombre.getText(),
                    (modelo.Ciudad) comboCiudad.getSelectedItem(), campoDireccion.getText());
            UtilidadesInterfaz.mostrarInfo(this, "Datos actualizados correctamente.");
            ventana.mostrarMenuAlojamiento();
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }

    private void modificarCapacidad() {
        Alojamiento alojamiento = (Alojamiento) ventana.getSesion();
        JTextField campoCapacidad = new JTextField(String.valueOf(alojamiento.getCapacidad()), 10);
        JPanel form = UtilidadesInterfaz.formulario(
                UtilidadesInterfaz.fila("Nueva capacidad:", campoCapacidad)
        );

        int opcion = JOptionPane.showConfirmDialog(this, form, "Modificar capacidad",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int capacidad = Integer.parseInt(campoCapacidad.getText().trim());
            ventana.getGestorAlojamientos().modificarCapacidad(alojamiento, capacidad);
            UtilidadesInterfaz.mostrarInfo(this, "Capacidad actualizada correctamente.");
            ventana.mostrarMenuAlojamiento();
        } catch (NumberFormatException ex) {
            UtilidadesInterfaz.mostrarError(this, "La capacidad debe ser un numero entero.");
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }

    private void consultarHuespedes() {
        Alojamiento alojamiento = (Alojamiento) ventana.getSesion();

        JTextField campoDesde = new JTextField("2026-11-08", 12);
        JTextField campoHasta = new JTextField("2026-11-11", 12);
        JPanel form = UtilidadesInterfaz.formulario(
                UtilidadesInterfaz.fila("Desde (AAAA-MM-DD):", campoDesde),
                UtilidadesInterfaz.fila("Hasta (AAAA-MM-DD):", campoHasta)
        );

        int opcion = JOptionPane.showConfirmDialog(this, form, "Consultar huespedes",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            LocalDate desde = UtilidadesInterfaz.leerFecha(campoDesde.getText());
            LocalDate hasta = UtilidadesInterfaz.leerFecha(campoHasta.getText());
            if (!hasta.isAfter(desde)) {
                UtilidadesInterfaz.mostrarError(this, "La fecha final debe ser posterior a la inicial.");
                return;
            }

            List<Reserva> huespedes = ventana.getGestorAlojamientos()
                    .huespedesEnPeriodo(alojamiento, desde, hasta);

            String[] columnas = {"Asistente", "Ingreso", "Egreso"};
            Object[][] filas = new Object[huespedes.size()][3];
            for (int i = 0; i < huespedes.size(); i++) {
                Reserva r = huespedes.get(i);
                filas[i] = new Object[]{r.getAsistente().getNombre(), r.getIngreso(), r.getEgreso()};
            }

            JTable tabla = new JTable(UtilidadesInterfaz.modeloTabla(columnas, filas));
            JOptionPane.showMessageDialog(this, UtilidadesInterfaz.tabla(tabla),
                    "Huespedes alojados (" + desde + " a " + hasta + ")",
                    JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }

    private JButton botonCentrado(String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, boton.getPreferredSize().height));
        return boton;
    }
}
