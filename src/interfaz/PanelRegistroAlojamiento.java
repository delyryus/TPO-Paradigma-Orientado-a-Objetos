package interfaz;

import modelo.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * Formulario de registro de un alojamiento nuevo (parroquia, hotel o espacio
 * de organizacion). Muestra el campo extra segun el tipo elegido.
 */
public class PanelRegistroAlojamiento extends JPanel {

    private final VentanaPrincipal ventana;
    private final JTextField campoNombreUsuario;
    private final JPasswordField campoClave;
    private final JTextField campoNombre;
    private final JComboBox<Ciudad> comboCiudad;
    private final JTextField campoDireccion;
    private final JTextField campoCapacidad;
    private final JComboBox<String> comboTipo;
    private final JPanel panelExtra;
    private final JTextField campoCategoria;
    private final JTextField campoOrganizador;
    private final JTextField campoResponsable;
    private final JLabel etiquetaExtra;

    public PanelRegistroAlojamiento(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setLayout(new BorderLayout());
        add(UtilidadesInterfaz.titulo("Registro de alojamiento"), BorderLayout.NORTH);

        campoNombreUsuario = new JTextField(16);
        campoClave = new JPasswordField(16);
        campoNombre = new JTextField(16);
        comboCiudad = new JComboBox<>(Ciudad.values());
        campoDireccion = new JTextField(16);
        campoCapacidad = new JTextField(16);
        comboTipo = new JComboBox<>(new String[]{"PARROQUIA", "HOTEL", "ESPACIO_ORG"});

        campoCategoria = new JTextField(8);
        campoOrganizador = new JTextField(16);
        campoResponsable = new JTextField(16);
        etiquetaExtra = new JLabel();

        panelExtra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboTipo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                actualizarCampoExtra();
            }
        });
        actualizarCampoExtra();

        JPanel form = UtilidadesInterfaz.formulario(
                UtilidadesInterfaz.fila("Tipo de alojamiento:", comboTipo),
                UtilidadesInterfaz.fila("Nombre de usuario:", campoNombreUsuario),
                UtilidadesInterfaz.fila("Clave:", campoClave),
                UtilidadesInterfaz.fila("Nombre:", campoNombre),
                UtilidadesInterfaz.fila("Ciudad:", comboCiudad),
                UtilidadesInterfaz.fila("Direccion:", campoDireccion),
                UtilidadesInterfaz.fila("Capacidad:", campoCapacidad)
        );
        form.add(panelExtra);

        add(form, BorderLayout.CENTER);

        JButton botonRegistrar = UtilidadesInterfaz.boton("Registrar", e -> registrar());
        JButton botonVolver = UtilidadesInterfaz.boton("Volver", e -> ventana.mostrarMenuPrincipal());
        add(UtilidadesInterfaz.panelBotones(botonRegistrar, botonVolver), BorderLayout.SOUTH);
    }

    private void actualizarCampoExtra() {
        panelExtra.removeAll();
        String tipo = (String) comboTipo.getSelectedItem();
        if ("HOTEL".equals(tipo)) {
            etiquetaExtra.setText("Categoria (1 a 5):");
            panelExtra.add(etiquetaExtra);
            panelExtra.add(campoCategoria);
        } else if ("ESPACIO_ORG".equals(tipo)) {
            etiquetaExtra.setText("Organizador:");
            panelExtra.add(etiquetaExtra);
            panelExtra.add(campoOrganizador);
        } else {
            etiquetaExtra.setText("Responsable:");
            panelExtra.add(etiquetaExtra);
            panelExtra.add(campoResponsable);
        }
        panelExtra.revalidate();
        panelExtra.repaint();
    }

    private void registrar() {
        try {
            String nombreUsuario = campoNombreUsuario.getText().trim();
            String clave = new String(campoClave.getPassword());
            String nombre = campoNombre.getText().trim();
            Ciudad ciudad = (Ciudad) comboCiudad.getSelectedItem();
            String direccion = campoDireccion.getText().trim();
            int capacidad = Integer.parseInt(campoCapacidad.getText().trim());

            if (nombreUsuario.isEmpty() || clave.isEmpty() || nombre.isEmpty() || direccion.isEmpty()) {
                UtilidadesInterfaz.mostrarError(this, "Completa todos los campos.");
                return;
            }

            Alojamiento alojamiento;
            String tipo = (String) comboTipo.getSelectedItem();
            switch (tipo) {
                case "PARROQUIA":
                    String responsable = campoResponsable.getText().trim();
                    if (responsable.isEmpty()) {
                        throw new IllegalArgumentException("El responsable es obligatorio.");
                    }
                    alojamiento = new Parroquia(nombreUsuario, clave, nombre, ciudad, direccion, capacidad, responsable);
                    break;
                case "HOTEL":
                    int categoria = Integer.parseInt(campoCategoria.getText().trim());
                    alojamiento = new Hotel(nombreUsuario, clave, nombre, ciudad, direccion, capacidad, categoria);
                    break;
                case "ESPACIO_ORG":
                    String organizador = campoOrganizador.getText().trim();
                    if (organizador.isEmpty()) {
                        throw new IllegalArgumentException("El organizador es obligatorio.");
                    }
                    alojamiento = new EspacioOrg(nombreUsuario, clave, nombre, ciudad, direccion, capacidad, organizador);
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de alojamiento invalido.");
            }

            ventana.getGestorUsuarios().registrarAlojamiento(alojamiento);
            UtilidadesInterfaz.mostrarInfo(this, "Alojamiento registrado correctamente.");
            ventana.mostrarMenuPrincipal();
        } catch (NumberFormatException ex) {
            UtilidadesInterfaz.mostrarError(this, "La capacidad y la categoria deben ser numeros enteros.");
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }
}
