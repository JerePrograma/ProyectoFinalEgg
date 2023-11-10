package com.proyectofinal.servicios;

import com.proyectofinal.entidades.Imagen;
import com.proyectofinal.entidades.Inmueble;
import com.proyectofinal.excepciones.MiExcepcion;
import com.proyectofinal.repositorios.ImagenRepositorio;
import com.proyectofinal.repositorios.InmuebleRepositorio;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class InmuebleServicio {

    @Autowired
    private InmuebleRepositorio inmuebleRepositorio;

    @Autowired
    private ImagenServicio imagenServicio;

    @Autowired
    private RangoHorarioServicio rangoHorarioServicio;

    @Autowired
    private ImagenRepositorio imagenRepositorio;

    @Transactional
    public Inmueble registrarInmueble(MultipartFile archivoPrincipal, MultipartFile[] archivosSecundarios,
            String cuentaTributaria, String direccion, String ciudad, String provincia,
            String transaccion, String tipoInmueble, String tituloAnuncio,
            String descripcionAnuncio, Integer precioAlquilerVenta,
            String caracteristicaInmueble, String estado) throws Exception {

        // Validar datos de entrada
        validarDatos(archivoPrincipal, cuentaTributaria, direccion, ciudad, provincia, transaccion, tipoInmueble,
                tituloAnuncio, descripcionAnuncio, precioAlquilerVenta, caracteristicaInmueble, estado);

        // Crear y configurar el inmueble
        Inmueble inmueble = new Inmueble();
        inmueble.setCuentaTributaria(cuentaTributaria);
        inmueble.setDireccion(direccion);
        inmueble.setCiudad(ciudad);
        inmueble.setProvincia(provincia);
        inmueble.setTransaccion(transaccion);
        inmueble.setTipoInmueble(tipoInmueble);
        inmueble.setDescripcionAnuncio(descripcionAnuncio);
        inmueble.setPrecioAlquilerVenta(precioAlquilerVenta);
        inmueble.setCaracteristicaInmueble(caracteristicaInmueble);
        inmueble.setEstado(estado);
        inmueble.setTituloAnuncio(tituloAnuncio);
        inmueble.setAlta(true);

        // Guardar el inmueble para generar su ID
        inmueble = inmuebleRepositorio.save(inmueble);

        // Guardar la imagen principal y asociarla con el inmueble
        if (!archivoPrincipal.isEmpty()) {
            Imagen imagenPrincipal = imagenServicio.guardarImagen(archivoPrincipal);
            imagenPrincipal.setInmueble(inmueble);
            imagenPrincipal = imagenRepositorio.save(imagenPrincipal);
            inmueble.setImagenPrincipal(imagenPrincipal);
        }

        // Guardar las imágenes secundarias y asociarlas con el inmueble
        List<Imagen> imagenesSecundarias = new ArrayList<>();
        for (MultipartFile archivoSecundario : archivosSecundarios) {
            if (!archivoSecundario.isEmpty()) {
                Imagen imagenSecundaria = imagenServicio.guardarImagen(archivoSecundario);
                imagenSecundaria.setInmueble(inmueble);
                imagenSecundaria = imagenRepositorio.save(imagenSecundaria);
                imagenesSecundarias.add(imagenSecundaria);
            }
        }
        inmueble.setImagenesSecundarias(imagenesSecundarias);

        // No es necesario guardar de nuevo el inmueble si los métodos de guardarImagen ya realizan el guardado
        return inmueble; // Devolver el inmueble guardado con las relaciones establecidas
    }

    @Transactional
    public void modificarInmueble(String cuentaTributaria, MultipartFile archivoPrincipal, MultipartFile[] archivosSecundarios,
            String tituloAnuncio, String descripcionAnuncio, String caracteristicaInmueble, String estado) throws Exception {
        validarDatosModificar(cuentaTributaria, tituloAnuncio, descripcionAnuncio, caracteristicaInmueble, estado);

        Optional<Inmueble> respuesta = inmuebleRepositorio.findById(cuentaTributaria);
        if (respuesta.isPresent()) {
            Inmueble inmueble = respuesta.get();

            if (archivoPrincipal != null) {
                // Guardar la nueva imagen principal
                Imagen nuevaImagenPrincipal = imagenServicio.guardarImagen(archivoPrincipal);
                inmueble.setImagenPrincipal(nuevaImagenPrincipal);
            }

            if (archivosSecundarios != null) {
                // Eliminar las imágenes secundarias antiguas
                List<Imagen> imagenesAntiguas = inmueble.getImagenesSecundarias();
                for (Imagen imagenAntigua : imagenesAntiguas) {
                    imagenServicio.eliminarImagen(imagenAntigua.getId());
                }

                // Guardar las nuevas imágenes secundarias
                List<Imagen> imagenesNuevas = new ArrayList<>();
                for (MultipartFile archivo : archivosSecundarios) {
                    Imagen imagen = imagenServicio.guardarImagen(archivoPrincipal);
                    imagenesNuevas.add(imagen);
                }
                inmueble.setImagenesSecundarias(imagenesNuevas);
            }

            inmueble.setTituloAnuncio(tituloAnuncio);
            inmueble.setDescripcionAnuncio(descripcionAnuncio);
            inmueble.setCaracteristicaInmueble(caracteristicaInmueble);
            inmueble.setEstado(estado);

            inmuebleRepositorio.save(inmueble);
        } else {
            throw new MiExcepcion("No se ha encontrado un inmueble con la cuenta tributaria proporcionada.");
        }
    }

    public Inmueble getOne(String cuentaTributaria) {
        return inmuebleRepositorio.getOne(cuentaTributaria);
    }

    @Transactional(readOnly = true)
    public List<Inmueble> listarTodosLosInmuebles() {
        // Implementa la lógica para listar todos los inmuebles
        return inmuebleRepositorio.findAll();
    }

    @Transactional
    public void darBajaInmueble(String cuentaTributaria) {
        Optional<Inmueble> respuesta = inmuebleRepositorio.findById(cuentaTributaria);
        if (respuesta.isPresent()) {
            Inmueble inmueble = respuesta.get();
            inmueble.setAlta(false);

            inmuebleRepositorio.save(inmueble);
        }
    }

    @Transactional
    public void darAltaInmueble(String cuentaTributaria) {
        Optional<Inmueble> respuesta = inmuebleRepositorio.findById(cuentaTributaria);
        if (respuesta.isPresent()) {
            Inmueble inmueble = respuesta.get();
            inmueble.setAlta(true);

            inmuebleRepositorio.save(inmueble);
        }
    }

    @Transactional
    public void eliminarInmueblePorCuentaTributaria(String cuentaTributaria) {
        // Implementa la lógica para eliminar un inmueble por su cuenta tributaria
        inmuebleRepositorio.deleteById(cuentaTributaria);
    }

    public List<Inmueble> buscarInmueblesPorFiltros(String ubicacion, String transaccion, String tipoInmueble, String ciudad, String provincia) {
        // Aquí construyes y ejecutas tu consulta personalizada utilizando el repositorio de InmuebleRepositorio.
        // Puedes usar las cláusulas WHERE en la consulta para aplicar los filtros necesarios.

        // Ejemplo de consulta con el uso de InmuebleRepositorio:
        return inmuebleRepositorio.findInmueblesByFiltros(ubicacion, transaccion, tipoInmueble, ciudad, provincia);
    }

    public Inmueble obtenerInmueblePorCuentaTributaria(String cuentaTributaria) {
        // Implementa la lógica para obtener un inmueble por su cuenta tributaria
        return inmuebleRepositorio.findById(cuentaTributaria).orElse(null);
    }

    public void validarDatos(MultipartFile archivo, String cuentaTributaria, String direccion, String ciudad, String provincia,
            String transaccion, String tipoInmueble, String tituloAnuncio,
            String descripcionAnuncio, Integer precioAlquilerVenta, String caracteristicaInmueble, String estado) throws MiExcepcion {
        if (archivo == null || archivo.isEmpty()) {
            throw new MiExcepcion("La imagen no puede estar vacío o ser nulo");
        }
        if (cuentaTributaria == null || cuentaTributaria.isEmpty()) {
            throw new MiExcepcion("El cuentaTributaria no puede estar vacío o ser nulo");
        }
        if (transaccion == null || transaccion.isEmpty()) {
            throw new MiExcepcion("El transaccion no puede estar vacío o ser nulo");
        }
        if (direccion == null || direccion.isEmpty()) {
            throw new MiExcepcion("El direccion no puede estar vacío o ser nulo");
        }
        if (ciudad == null || ciudad.isEmpty()) {
            throw new MiExcepcion("El ciudad no puede estar vacío o ser nulo");
        }
        if (provincia == null || provincia.isEmpty()) {
            throw new MiExcepcion("El código no puede estar vacío o ser nulo");
        }
        if (tipoInmueble == null) {
            throw new MiExcepcion("El tipoInmueble no puede estar vacío o ser nulo");
        }
        if (tituloAnuncio == null || tituloAnuncio.isEmpty()) {
            throw new MiExcepcion("El tituloAnuncio no puede estar vacío o ser nulo");
        }
        if (descripcionAnuncio == null || descripcionAnuncio.isEmpty()) {
            throw new MiExcepcion("El descripcionAnuncio no puede estar vacío o ser nulo");
        }

        if (precioAlquilerVenta == null || precioAlquilerVenta <= 0) {
            throw new MiExcepcion("El tituloAnuncio no puede estar vacío o ser nulo");
        }
        if (caracteristicaInmueble == null || caracteristicaInmueble.isEmpty()) {
            throw new MiExcepcion("El caracteristicaInmueble no puede estar vacío o ser nulo");
        }
        if (estado == null) {
            throw new MiExcepcion("El estado no puede estar vacío o ser nulo");
        }
    }

    public void validarDatosModificar(String cuentaTributaria,
            String tituloAnuncio, String descripcionAnuncio, String caracteristicaInmueble, String estado) throws MiExcepcion {
        if (cuentaTributaria == null || cuentaTributaria.isEmpty()) {
            throw new MiExcepcion("El cuentaTributaria no puede estar vacío o ser nulo");
        }
        if (tituloAnuncio == null || tituloAnuncio.isEmpty()) {
            throw new MiExcepcion("El tituloAnuncio no puede estar vacío o ser nulo");
        }
        if (descripcionAnuncio == null || descripcionAnuncio.isEmpty()) {
            throw new MiExcepcion("El descripcionAnuncio no puede estar vacío o ser nulo");
        }

        if (caracteristicaInmueble == null || caracteristicaInmueble.isEmpty()) {
            throw new MiExcepcion("El caracteristicaInmueble no puede estar vacío o ser nulo");
        }
        if (estado == null) {
            throw new MiExcepcion("El estado no puede estar vacío o ser nulo");
        }
    }

}
