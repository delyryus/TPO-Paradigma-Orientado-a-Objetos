package interfaz;

import modelo.Ciudad;
import modelo.Evento;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Menu del administrador. Permite registrar, modificar y consultar eventos.
 */
public class PanelAdministrador extends JPanel {

    private final VentanaPrincipal ventana;

    public PanelAdministrador(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setLayout(new BorderLayout());
        add(UtilidadesInterfaz.titulo("Menu de administrador"), BorderLayout.NORTH);

        JPanel botones = new JPanel();
        botones.setLayout(new BoxLayout(botones, BoxLayout.Y_AXIS));
        botones.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        botones.add(botonCentrado("Registrar evento", e -> registrarEvento()));
        botones.add(botonCentrado("Modificar evento", e -> modificarEvento()));
        botones.add(botonCentrado("Consultar eventos", e -> ventana.mostrarAgenda()));

        add(botones, BorderLayout.CENTER);

        JButton botonCerrarSesion = UtilidadesInterfaz.boton("Cerrar sesion", e -> ventana.cerrarSesion());
        JButton botonSalir = UtilidadesInterfaz.boton("Salir", e -> System.exit(0));
        add(UtilidadesInterfaz.panelBotones(botonCerrarSesion, botonSalir), BorderLayout.SOUTH);
    }

    private void registrarEvento() {
        JTextField campoFecha = new JTextField("2026-11-08", 12);
        JTextField campoHorario = new JTextField("10:00", 12);
        JTextField campoActividad = new JTextField("", 20);
        JComboBox<Ciudad> comboCiudad = new JComboBox<>(Ciudad.values());

        JPanel form = UtilidadesInterfaz.formulario(
                UtilidadesInterfaz.fila("Fecha (AAAA-MM-DD):", campoFecha),
                UtilidadesInterfaz.fila("Horario (HH:mm):", campoHorario),
                UtilidadesInterfaz.fila("Actividad:", campoActividad),
                UtilidadesInterfaz.fila("Ciudad:", comboCiudad)
        );

        int opcion = JOptionPane.showConfirmDialog(this, form, "Registrar evento",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            ventana.getGestorEventos().registrar(
                    UtilidadesInterfaz.leerFecha(campoFecha.getText()),
                    UtilidadesInterfaz.leerHora(campoHorario.getText()),
                    campoActividad.getText(),
                    (Ciudad) comboCiudad.getSelectedItem());
            UtilidadesInterfaz.mostrarInfo(this, "Evento registrado correctamente.");
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }

    private void modificarEvento() {
        List<Evento> eventos = ventana.getGestorEventos().listarTodosOrdenados();
        if (eventos.isEmpty()) {
            UtilidadesInterfaz.mostrarInfo(this, "No hay eventos registrados para modificar.");
            return;
        }

        JComboBox<String> comboEventos = new JComboBox<>();
        for (Evento e : eventos) {
            comboEventos.addItem(etiquetaEvento(e));
        }

        JPanel formId = UtilidadesInterfaz.formulario(
                UtilidadesInterfaz.fila("Evento:", comboEventos)
        );
        int opcionId = JOptionPane.showConfirmDialog(this, formId, "Modificar evento - paso 1",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcionId != JOptionPane.OK_OPTION) {
            return;
        }

        Evento evento = eventos.get(comboEventos.getSelectedIndex());

        JTextField campoFecha = new JTextField(evento.getFecha().toString(), 12);
        JTextField campoHorario = new JTextField(evento.getHorario().toString(), 12);
        JTextField campoActividad = new JTextField(evento.getActividad(), 20);
        JComboBox<Ciudad> comboCiudad = new JComboBox<>(Ciudad.values());
        comboCiudad.setSelectedItem(evento.getCiudad());

        JPanel form = UtilidadesInterfaz.formulario(
                UtilidadesInterfaz.fila("Fecha (AAAA-MM-DD):", campoFecha),
                UtilidadesInterfaz.fila("Horario (HH:mm):", campoHorario),
                UtilidadesInterfaz.fila("Actividad:", campoActividad),
                UtilidadesInterfaz.fila("Ciudad:", comboCiudad)
        );

        int opcion = JOptionPane.showConfirmDialog(this, form, "Modificar evento - paso 2",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            ventana.getGestorEventos().modificarEvento(evento,
                    UtilidadesInterfaz.leerFecha(campoFecha.getText()),
                    UtilidadesInterfaz.leerHora(campoHorario.getText()),
                    campoActividad.getText(),
                    (Ciudad) comboCiudad.getSelectedItem());
            UtilidadesInterfaz.mostrarInfo(this, "Evento modificado correctamente.");
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }

    private String etiquetaEvento(Evento evento) {
        return evento.getFecha() + " " + evento.getHorario() + " - "
                + evento.getActividad() + " (" + evento.getCiudad() + ")";
    }

    private JButton botonCentrado(String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, boton.getPreferredSize().height));
        return boton;
    }
}
