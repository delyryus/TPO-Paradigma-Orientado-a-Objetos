package persistencia;

import modelo.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Se encarga de cargar y guardar los tres archivos JSON de la aplicacion.
 * La escritura se realiza con StringBuilder y la lectura con el ParserJSON propio.
 *
 * El modelo usa referencias directas entre objetos (el UML no modela ids), por
 * lo que en los archivos JSON las cuentas se referencian por nombre de usuario
 * (unico) y los eventos inscritos se guardan con sus datos completos.
 *
 * NOTA: las claves se guardan en texto plano exclusivamente por la limitacion
 * academica del TPO. Esto no es una practica segura para produccion.
 */
public class PersistenciaJSON {

    /** Carpeta donde se guardan los archivos de datos. Relativa a la raiz del proyecto. */
    private final String directorio;

    public PersistenciaJSON() {
        this.directorio = "datos";
    }

    /** Constructor que permite indicar otra carpeta de datos (util en pruebas). */
    public PersistenciaJSON(String directorio) {
        this.directorio = directorio;
    }

    // ------------------------------------------------------------------ //
    // CUENTAS                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Carga las cuentas y resuelve las referencias a los eventos ya cargados.
     */
    public List<Usuario> cargarCuentas(List<Evento> eventos) throws IOException {
        Object raiz = parsearArchivo("cuentas.json");
        if (!(raiz instanceof List)) {
            throw new IOException("Formato invalido en cuentas.json: se esperaba un arreglo.");
        }
        List<Usuario> cuentas = new ArrayList<>();
        for (Object elemento : (List<?>) raiz) {
            cuentas.add(construirUsuario((Map<String, Object>) elemento, eventos));
        }
        return cuentas;
    }

    public void guardarCuentas(List<Usuario> cuentas) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < cuentas.size(); i++) {
            sb.append(usuarioJson(cuentas.get(i)));
            if (i < cuentas.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");
        escribirArchivo("cuentas.json", sb.toString());
    }

    // ------------------------------------------------------------------ //
    // EVENTOS                                                             //
    // ------------------------------------------------------------------ //

    public List<Evento> cargarEventos() throws IOException {
        Object raiz = parsearArchivo("eventos.json");
        if (!(raiz instanceof List)) {
            throw new IOException("Formato invalido en eventos.json: se esperaba un arreglo.");
        }
        List<Evento> eventos = new ArrayList<>();
        for (Object elemento : (List<?>) raiz) {
            eventos.add(construirEvento((Map<String, Object>) elemento));
        }
        return eventos;
    }

    public void guardarEventos(List<Evento> eventos) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < eventos.size(); i++) {
            sb.append("  ").append(eventoJson(eventos.get(i)));
            if (i < eventos.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");
        escribirArchivo("eventos.json", sb.toString());
    }

    // ------------------------------------------------------------------ //
    // RESERVAS                                                            //
    // ------------------------------------------------------------------ //

    /**
     * Carga las reservas y las adjunta a la coleccion del alojamiento que
     * corresponde. Las cuentas se referencian por nombre de usuario.
     */
    public List<Reserva> cargarReservas(List<Usuario> usuarios) throws IOException {
        Object raiz = parsearArchivo("reservas.json");
        if (!(raiz instanceof List)) {
            throw new IOException("Formato invalido en reservas.json: se esperaba un arreglo.");
        }
        List<Reserva> reservas = new ArrayList<>();
        for (Object elemento : (List<?>) raiz) {
            Map<String, Object> m = (Map<String, Object>) elemento;
            String nombreAsistente = (String) m.get("asistente");
            String nombreAlojamiento = (String) m.get("alojamiento");
            Asistente asistente = buscarAsistente(usuarios, nombreAsistente);
            Alojamiento alojamiento = buscarAlojamiento(usuarios, nombreAlojamiento);
            if (asistente == null) {
                throw new IOException("reservas.json referencia a un asistente inexistente: " + nombreAsistente);
            }
            if (alojamiento == null) {
                throw new IOException("reservas.json referencia a un alojamiento inexistente: " + nombreAlojamiento);
            }
            Reserva reserva = new Reserva(asistente,
                    LocalDate.parse((String) m.get("ingreso")),
                    LocalDate.parse((String) m.get("egreso")),
                    EstadoReserva.valueOf((String) m.get("estado")));
            alojamiento.getReservas().add(reserva);
            reservas.add(reserva);
        }
        return reservas;
    }

    /**
     * Guarda todas las reservas de todos los alojamientos. El nombre del
     * alojamiento se obtiene de la coleccion que las contiene, porque la
     * asociacion es navegable desde Alojamiento hacia Reserva.
     */
    public void guardarReservas(List<Alojamiento> alojamientos) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        boolean primero = true;
        for (Alojamiento alojamiento : alojamientos) {
            for (Reserva r : alojamiento.getReservas()) {
                if (!primero) {
                    sb.append(",\n");
                }
                primero = false;
                sb.append("  { \"asistente\": ").append(jsonCadena(r.getAsistente().getNombreUsuario()));
                sb.append(", \"alojamiento\": ").append(jsonCadena(alojamiento.getNombreUsuario()));
                sb.append(", \"ingreso\": ").append(jsonCadena(r.getIngreso().toString()));
                sb.append(", \"egreso\": ").append(jsonCadena(r.getEgreso().toString()));
                sb.append(", \"estado\": ").append(jsonCadena(r.getEstado().name()));
                sb.append(" }");
            }
        }
        if (!primero) {
            sb.append("\n");
        }
        sb.append("]\n");
        escribirArchivo("reservas.json", sb.toString());
    }

    // ------------------------------------------------------------------ //
    // CONSTRUCCION DE OBJETOS DESDE JSON                                  //
    // ------------------------------------------------------------------ //

    private Usuario construirUsuario(Map<String, Object> m, List<Evento> eventos) throws IOException {
        String tipo = (String) m.get("tipo");
        String nombreUsuario = (String) m.get("nombreUsuario");
        String clave = (String) m.get("clave");
        String nombre = (String) m.get("nombre");

        switch (tipo) {
            case "ADMINISTRADOR":
                return new Administrador(nombreUsuario, clave, nombre);
            case "ASISTENTE": {
                String dni = (String) m.get("dni");
                Ciudad origen = Ciudad.valueOf((String) m.get("origen"));
                Asistente asistente = new Asistente(nombreUsuario, clave, nombre, dni, origen);
                Object inscripciones = m.get("eventos");
                if (inscripciones instanceof List) {
                    for (Object item : (List<?>) inscripciones) {
                        Map<String, Object> datosEvento = (Map<String, Object>) item;
                        Evento evento = buscarEvento(eventos, datosEvento);
                        if (evento == null) {
                            throw new IOException("cuentas.json referencia a un evento inexistente: "
                                    + datosEvento.get("fecha") + " " + datosEvento.get("horario")
                                    + " " + datosEvento.get("actividad"));
                        }
                        asistente.inscribir(evento);
                    }
                }
                return asistente;
            }
            case "PARROQUIA":
                return new Parroquia(nombreUsuario, clave, nombre, ciudad(m), direccion(m), capacidad(m),
                        texto(m.get("responsable")));
            case "HOTEL":
                return new Hotel(nombreUsuario, clave, nombre, ciudad(m), direccion(m), capacidad(m),
                        numero(m.get("categoria")).intValue());
            case "ESPACIO_ORG":
                return new EspacioOrg(nombreUsuario, clave, nombre, ciudad(m), direccion(m), capacidad(m),
                        (String) m.get("organizador"));
            default:
                throw new IOException("Tipo de cuenta desconocido en cuentas.json: " + tipo);
        }
    }

    private Evento construirEvento(Map<String, Object> m) throws IOException {
        return new Evento(
                LocalDate.parse((String) m.get("fecha")),
                LocalTime.parse((String) m.get("horario")),
                (String) m.get("actividad"),
                Ciudad.valueOf((String) m.get("ciudad")));
    }

    private Evento buscarEvento(List<Evento> eventos, Map<String, Object> datos) throws IOException {
        LocalDate fecha = LocalDate.parse((String) datos.get("fecha"));
        LocalTime horario = LocalTime.parse((String) datos.get("horario"));
        String actividad = (String) datos.get("actividad");
        Ciudad ciudad = Ciudad.valueOf((String) datos.get("ciudad"));
        for (Evento e : eventos) {
            if (e.getFecha().equals(fecha) && e.getHorario().equals(horario)
                    && e.getActividad().equals(actividad) && e.getCiudad() == ciudad) {
                return e;
            }
        }
        return null;
    }

    private Asistente buscarAsistente(List<Usuario> usuarios, String nombreUsuario) {
        for (Usuario u : usuarios) {
            if (u instanceof Asistente && u.getNombreUsuario().equals(nombreUsuario)) {
                return (Asistente) u;
            }
        }
        return null;
    }

    private Alojamiento buscarAlojamiento(List<Usuario> usuarios, String nombreUsuario) {
        for (Usuario u : usuarios) {
            if (u instanceof Alojamiento && u.getNombreUsuario().equals(nombreUsuario)) {
                return (Alojamiento) u;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ //
    // ESCRITURA DEL JSON DE USUARIOS                                      //
    // ------------------------------------------------------------------ //

    private String usuarioJson(Usuario u) {
        StringBuilder sb = new StringBuilder();
        sb.append("  { ");
        sb.append("\"tipo\": ").append(jsonCadena(tipoUsuario(u)));
        sb.append(", \"nombreUsuario\": ").append(jsonCadena(u.getNombreUsuario()));
        sb.append(", \"clave\": ").append(jsonCadena(u.getClave()));
        sb.append(", \"nombre\": ").append(jsonCadena(u.getNombre()));

        if (u instanceof Asistente) {
            Asistente a = (Asistente) u;
            sb.append(", \"dni\": ").append(jsonCadena(a.getDni()));
            sb.append(", \"origen\": ").append(jsonCadena(a.getOrigen().name()));
            sb.append(", \"eventos\": [");
            List<Evento> eventos = a.getEventos();
            for (int i = 0; i < eventos.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(eventoJson(eventos.get(i)));
            }
            sb.append("]");
        } else if (u instanceof Alojamiento) {
            Alojamiento al = (Alojamiento) u;
            sb.append(", \"ciudad\": ").append(jsonCadena(al.getCiudad().name()));
            sb.append(", \"direccion\": ").append(jsonCadena(al.getDireccion()));
            sb.append(", \"capacidad\": ").append(al.getCapacidad());
            if (al instanceof Parroquia) {
                sb.append(", \"responsable\": ").append(jsonCadena(((Parroquia) al).getResponsable()));
            } else if (al instanceof Hotel) {
                sb.append(", \"categoria\": ").append(((Hotel) al).getCategoria());
            } else if (al instanceof EspacioOrg) {
                sb.append(", \"organizador\": ").append(jsonCadena(((EspacioOrg) al).getOrganizador()));
            }
        }
        sb.append(" }");
        return sb.toString();
    }

    /** Serializa un evento con todos sus datos (no tiene id en el modelo). */
    private String eventoJson(Evento e) {
        return "{ \"fecha\": " + jsonCadena(e.getFecha().toString())
                + ", \"horario\": " + jsonCadena(e.getHorario().toString())
                + ", \"actividad\": " + jsonCadena(e.getActividad())
                + ", \"ciudad\": " + jsonCadena(e.getCiudad().name()) + " }";
    }

    private String tipoUsuario(Usuario u) {
        if (u instanceof Administrador) {
            return "ADMINISTRADOR";
        }
        if (u instanceof Asistente) {
            return "ASISTENTE";
        }
        if (u instanceof Parroquia) {
            return "PARROQUIA";
        }
        if (u instanceof Hotel) {
            return "HOTEL";
        }
        if (u instanceof EspacioOrg) {
            return "ESPACIO_ORG";
        }
        throw new IllegalArgumentException("Tipo de usuario no soportado para guardar.");
    }

    // ------------------------------------------------------------------ //
    // UTILIDADES DE ACCESO A CAMPOS Y ARCHIVOS                            //
    // ------------------------------------------------------------------ //

    private Ciudad ciudad(Map<String, Object> m) {
        return Ciudad.valueOf((String) m.get("ciudad"));
    }

    private String direccion(Map<String, Object> m) {
        return (String) m.get("direccion");
    }

    private int capacidad(Map<String, Object> m) throws IOException {
        return numero(m.get("capacidad")).intValue();
    }

    /** Devuelve el texto indicado o una cadena vacia si el campo no existe. */
    private String texto(Object valor) {
        return valor == null ? "" : (String) valor;
    }

    private Number numero(Object valor) throws IOException {
        if (valor instanceof Number) {
            return (Number) valor;
        }
        throw new IOException("Se esperaba un numero en el archivo JSON pero se encontro: " + valor);
    }

    private Object parsearArchivo(String nombre) throws IOException {
        Path archivo = Paths.get(directorio, nombre);
        if (!Files.exists(archivo)) {
            throw new IOException("No se encontro el archivo " + archivo.toAbsolutePath() + ".");
        }
        String contenido = Files.readString(archivo, StandardCharsets.UTF_8).trim();
        if (contenido.isEmpty()) {
            throw new IOException("El archivo " + nombre + " esta vacio.");
        }
        try {
            return new ParserJSON().parsear(contenido);
        } catch (IllegalArgumentException e) {
            throw new IOException("Formato JSON invalido en " + nombre + ": " + e.getMessage());
        }
    }

    private void escribirArchivo(String nombre, String contenido) throws IOException {
        Path carpeta = Paths.get(directorio);
        if (!Files.exists(carpeta)) {
            Files.createDirectories(carpeta);
        }
        Files.writeString(Paths.get(directorio, nombre), contenido, StandardCharsets.UTF_8);
    }

    /**
     * Escapa una cadena para poder incluirla dentro de un JSON.
     */
    private String jsonCadena(String texto) {
        StringBuilder sb = new StringBuilder("\"");
        if (texto != null) {
            for (int i = 0; i < texto.length(); i++) {
                char c = texto.charAt(i);
                switch (c) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        if (c < 0x20) {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
