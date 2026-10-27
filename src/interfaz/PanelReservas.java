package interfaz;

import modelo.Alojamiento;
import modelo.Asistente;
import modelo.Ciudad;
import modelo.Reserva;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Gestion de reservas del asistente: muestra la reserva actual, permite
 * reservar un alojamiento (eligiendo ciudad, alojamiento y fechas, mostrando la
 * disponibilidad antes de confirmar) y cancelar la reserva vigente.
 */
public class PanelReservas extends JPanel {

    private final VentanaPrincipal ventana;
    private final JComboBox<Ciudad> comboCiudad;
    private final JComboBox<Alojamiento> comboAlojamiento;
    private final JTextField campoIngreso;
    private final JTextField campoEgreso;
    private final JLabel etiquetaDisponibilidad;
    private final JLabel etiquetaReservaActual;

    public PanelReservas(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setLayout(new BorderLayout());
        add(UtilidadesInterfaz.titulo("Mis reservas"), BorderLayout.NORTH);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        etiquetaReservaActual = new JLabel();
        etiquetaReservaActual.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        centro.add(etiquetaReservaActual);

        JButton botonCancelar = UtilidadesInterfaz.boton("Cancelar mi reserva", e -> cancelarReserva());
        centro.add(UtilidadesInterfaz.panelBotones(botonCancelar));
        centro.add(new JSeparator());

        comboCiudad = new JComboBox<>(Ciudad.values());
        comboAlojamiento = new JComboBox<>();
        campoIngreso = new JTextField("2026-11-08", 12);
        campoEgreso = new JTextField("2026-11-10", 12);
        etiquetaDisponibilidad = new JLabel(" ");

        comboCiudad.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                actualizarAlojamientos();
            }
        });
        actualizarAlojamientos();

        JPanel form = UtilidadesInterfaz.formulario(
                UtilidadesInterfaz.fila("Ciudad:", comboCiudad),
                UtilidadesInterfaz.fila("Alojamiento:", comboAlojamiento),
                UtilidadesInterfaz.fila("Ingreso (AAAA-MM-DD):", campoIngreso),
                UtilidadesInterfaz.fila("Egreso (AAAA-MM-DD):", campoEgreso)
        );
        centro.add(form);

        JButton botonVer = UtilidadesInterfaz.boton("Ver disponibilidad", e -> verDisponibilidad());
        JButton botonConfirmar = UtilidadesInterfaz.boton("Confirmar reserva", e -> confirmarReserva());
        centro.add(UtilidadesInterfaz.panelBotones(botonVer, botonConfirmar));

        etiquetaDisponibilidad.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        centro.add(etiquetaDisponibilidad);

        add(centro, BorderLayout.CENTER);

        JButton botonVolver = UtilidadesInterfaz.boton("Volver", e -> ventana.mostrarMenuAsistente());
        add(UtilidadesInterfaz.panelBotones(botonVolver), BorderLayout.SOUTH);

        actualizarReservaActual();
    }

    private Asistente asistente() {
        return (Asistente) ventana.getSesion();
    }

    private void actualizarReservaActual() {
        Reserva reserva = ventana.getGestorReservas().reservaActivaDe(asistente());
        if (reserva == null) {
            etiquetaReservaActual.setText("No tenes ninguna reserva activa.");
        } else {
            Alojamiento alojamiento = ventana.getGestorReservas().alojamientoDe(reserva);
            etiquetaReservaActual.setText("Reserva activa: " + alojamiento.getNombre()
                    + " del " + reserva.getIngreso() + " al " + reserva.getEgreso() + ".");
        }
    }

    private void actualizarAlojamientos() {
        comboAlojamiento.removeAllItems();
        Ciudad ciudad = (Ciudad) comboCiudad.getSelectedItem();
        List<Alojamiento> alojamientos = ventana.getGestorUsuarios().listarAlojamientosPorCiudad(ciudad);
        for (Alojamiento a : alojamientos) {
            comboAlojamiento.addItem(a);
        }
    }

    private void verDisponibilidad() {
        try {
            Alojamiento alojamiento = (Alojamiento) comboAlojamiento.getSelectedItem();
            if (alojamiento == null) {
                UtilidadesInterfaz.mostrarError(this, "No hay alojamientos en esa ciudad.");
                return;
            }
            LocalDate ingreso = UtilidadesInterfaz.leerFecha(campoIngreso.getText());
            LocalDate egreso = UtilidadesInterfaz.leerFecha(campoEgreso.getText());
            if (!egreso.isAfter(ingreso)) {
                throw new IllegalArgumentException("El egreso debe ser posterior al ingreso.");
            }
            int disponibles = ventana.getGestorReservas().disponibilidad(
                    alojamiento, ingreso, egreso);
            etiquetaDisponibilidad.setText("Disponibilidad para todo el periodo: "
                    + disponibles + " de " + alojamiento.getCapacidad() + " plazas.");
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }

    private void confirmarReserva() {
        try {
            Alojamiento alojamiento = (Alojamiento) comboAlojamiento.getSelectedItem();
            if (alojamiento == null) {
                UtilidadesInterfaz.mostrarError(this, "No hay alojamientos en esa ciudad.");
                return;
            }
            LocalDate ingreso = UtilidadesInterfaz.leerFecha(campoIngreso.getText());
            LocalDate egreso = UtilidadesInterfaz.leerFecha(campoEgreso.getText());

            ventana.getGestorReservas().crearReserva(asistente(), alojamiento, ingreso, egreso);
            UtilidadesInterfaz.mostrarInfo(this, "Reserva confirmada en " + alojamiento.getNombre() + ".");
            actualizarReservaActual();
            etiquetaDisponibilidad.setText(" ");
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }

    private void cancelarReserva() {
        Reserva reserva = ventana.getGestorReservas().reservaActivaDe(asistente());
        if (reserva == null) {
            UtilidadesInterfaz.mostrarError(this, "No tenes ninguna reserva activa.");
            return;
        }
        if (!UtilidadesInterfaz.confirmar(this, "Seguro que queres cancelar tu reserva?")) {
            return;
        }
        try {
            ventana.getGestorReservas().cancelarReserva(asistente());
            UtilidadesInterfaz.mostrarInfo(this, "Reserva cancelada.");
            actualizarReservaActual();
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }
}
