package com.gallerytv.mediaController.controller;

import com.gallerytv.mediaController.service.IMediaService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.net.MalformedURLException;
import java.util.List;

@OpenAPIDefinition(
        info = @Info(
            title = "Servidor de imagenes",
            contact = @Contact(
                name = "Contactame en LinkedIn",
                url = "https://www.linkedin.com/in/alejo-kannemann-10b58b191/"
        ),
        description = """
                Este es una api que almacena y devuelve imagenes.
                Estas tienen un periodo de almacenamiento hasta las 00:00 AM (GMT-3).
                No hace uso de base de datos para almacenar las rutas de las imagenes almacenadas.
                """,
        version = "1.0"),
        servers ={
                @Server(
                        url = "https://project-1.alejokannemann.com.ar/v1",
                        description = "Prod ENV"),
                @Server(
                        url = "http://localhost:8090",
                        description = "Test ENV"
                )
        } )
@RestController
@RequestMapping("images")
@Tag(name = "Servidor de Imagenes")
public class MediaController {

    @Autowired
    private IMediaService mediaService;

    @Operation(summary = "Utilice este endpoint para almacenar las imagenes en el servidor",
            description = """
                    Unicamente acepta archivos con extenciones \".jpg\", \".png\", \".gif\", \".jpeg\", \".svg\".
                    Para recuperar la imagen debe ir a la url que devuelve como respuesta el servidor.""",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "devuelve la url como respuesta, tendras que anotarla ya que no se hace uso de base de datos para la persistencia de las imagenes almacenadas en el servidor.",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(
                           responseCode = "400", description = "Fallo el guardado de la imagen, por favor asegurate que la extencion del archivo sea las permitidas por el servidor.",
                            content = @Content(schema = @Schema(implementation = String.class),examples = {
                                    @ExampleObject(name = "Extencion no permitida", value = "Solo aceptamos archivos con extensiones jpg, png, gif, jpeg y svg")
                            })
                    )
            }
    )
    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> subirArchivo(@ModelAttribute List<MultipartFile> images, HttpServletRequest request){
        return mediaService.uploadImage(images, request);
    }

    @Operation(summary = "Utilice este endpoint para recuperar la imagen almacenada en el servidor",
            description = "Recuerde que las imagenes vencen a las 00:00 AM (GMT-3).",
            hidden = true,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = Resource.class))),
                    @ApiResponse(
                            responseCode = "404", description = "No se encontro la imagen solicitada.",
                            content = @Content(schema = @Schema(implementation = String.class),examples = {
                                    @ExampleObject(name = "No se encontro la imagen", value = "No se encontró la imagen solicitada")
                            })
                    )
            }
    )
    @GetMapping("/{nombre}/**")
    public ResponseEntity<?> buscarArchivo(@PathVariable String nombre, HttpServletRequest request) throws MalformedURLException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (path != null) {
            path = path.substring(path.indexOf(nombre) + nombre.length());
            return mediaService.findImage(nombre+path);
        }
        return mediaService.findImage(nombre);
    }
}
