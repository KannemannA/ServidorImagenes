package com.gallerytv.mediaController.service.imp;

import com.gallerytv.mediaController.service.IMediaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class MediaService implements IMediaService {
    @Autowired
    private Executor taskExecutor;

    @Value("${media.location}")
    private String ubicacion;

    public List<String> subir(List<MultipartFile> images) {
        List<CompletableFuture<String>> futures = images.stream()
                .map(image -> CompletableFuture.supplyAsync(() -> {
                    try {
                        if (!Files.exists(Path.of(ubicacion))) {
                            Files.createDirectories(Path.of(ubicacion));
                        }
                        Path ruta = Path.of(ubicacion).resolve(Paths.get(Objects.requireNonNull(image.getOriginalFilename())))
                                .normalize().toAbsolutePath();
                        InputStream input = image.getInputStream();
                        Files.copy(input, ruta, StandardCopyOption.REPLACE_EXISTING);
                        return image.getOriginalFilename();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, taskExecutor))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> uploadImage(List<MultipartFile> images, HttpServletRequest request) {
        int val= 0;
        for (MultipartFile file : images){
            String ext= Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")+1);
            switch (ext){
                case "jpg", "png", "gif", "jpeg", "svg":
                    break;
                default:
                    val++;
            }
        }
        if (val == 0){
            List<String> name=subir(images);
            List<String> output= name.stream().map(arch -> "https://"+request.getServerName()+"/images/"+arch).toList();
            return new ResponseEntity<>(output, HttpStatus.OK);
        }
        return new ResponseEntity<>("Solo aceptamos archivos con extensiones jpg, png, gif, jpeg y svg",HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> findImage(String name) {
            try {
                Resource file = new UrlResource(Paths.get(ubicacion+"/"+name).toUri());
                if (file.exists()&& file.isReadable()) {
                    String fileExtension = Objects.requireNonNull(file.getFilename()).substring(file.getFilename().lastIndexOf('.') + 1);
                    MediaType mediaType = switch (fileExtension) {
                        case "jpg", "jfif", "pjpeg", "pjp" -> MediaType.IMAGE_JPEG;
                        case "svg" -> MediaType.parseMediaType("image/svg+xml");
                        default -> MediaType.parseMediaType("image/" + fileExtension);
                    };
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(mediaType);
                    headers.setLastModified(file.lastModified());
                    headers.set("Cache-Control", "max-age=86400");
                    headers.setContentDisposition(ContentDisposition.inline().filename(file.getFilename()).build());
                    headers.setETag("\"" + file.getFile().length() + "-" + file.getFile().lastModified() + "\"");
                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(file);
                }
                return new ResponseEntity<>("No se encontró la imagen solicitada", HttpStatus.NOT_FOUND);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
}
