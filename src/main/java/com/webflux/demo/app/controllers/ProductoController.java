package com.webflux.demo.app.controllers;

import com.webflux.demo.app.models.documents.Producto;
import com.webflux.demo.app.models.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);
    public static final String API_PRODUCTOS_BASIC_URI = "/api/productos/";
    public static final String PATH_UPLOADS = "${config.uploads.path}";
    public static final String PATH_BASICFINDALL = "/basicfindall";
    public static final String PATH_RESPONSEENTITYFINDALL = "/responseentityfindall";

    @Value(PATH_UPLOADS)
    private String uploadsPath;

    private final ProductoService productoService;


    @GetMapping(PATH_BASICFINDALL)
    public Flux<Producto> listar() {
        return productoService.findAll();
    }

    @GetMapping(PATH_RESPONSEENTITYFINDALL)
    public Mono<ResponseEntity<Flux<Producto>>> listarResponseENtity() {
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productoService.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> encontrarPorId(@PathVariable String id) {
        return productoService.findById(id)
                .map(
                        productoEncontrado -> ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(productoEncontrado)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Producto>> crear(@RequestBody Producto producto) {

        if (producto.getCreateAt() == null) {
            producto.setCreateAt(new Date());
        }

        return productoService.save(producto)
                .map(productoCreado -> ResponseEntity
                        .created(URI.create(API_PRODUCTOS_BASIC_URI.concat(productoCreado.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productoCreado)
                );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> editar(@RequestBody Producto producto, @PathVariable String id) {
        return productoService.findById(id)
                .flatMap(productoAEditar -> {

                    productoAEditar.setNombre(producto.getNombre());
                    productoAEditar.setPrecio(producto.getPrecio());
                    productoAEditar.setCategoria(producto.getCategoria());

                    return productoService.save(productoAEditar);
                })
                .map(
                        productoEditado -> ResponseEntity
                                .created(URI.create(API_PRODUCTOS_BASIC_URI.concat(productoEditado.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(productoEditado)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminarPorId(@PathVariable String id) {
        return productoService.findById(id)
                .flatMap(
                        productoEncontrado -> {
                            return productoService.delete(productoEncontrado)
                                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
                        }
                )
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/uploads/{id}")
    public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file) {
        return productoService.findById(id)
                .flatMap(
                        productoEncontrado -> {
                            productoEncontrado.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", ""));

                            return file.transferTo(new File(uploadsPath + productoEncontrado.getFoto()))
                                    .then(productoService.save(productoEncontrado));
                        }
                )
                .map(productoGuardado -> ResponseEntity.ok(productoGuardado))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/crear-producto-con-archivo")
    public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto, @RequestPart FilePart file) {

        if (producto.getCreateAt() == null) {
            producto.setCreateAt(new Date());
        }

        producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", ""));

        return file.transferTo(new File(uploadsPath + producto.getFoto()))
                .then(productoService.save(producto))
                .map(productoCreado -> ResponseEntity
                        .created(URI.create(API_PRODUCTOS_BASIC_URI.concat(productoCreado.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productoCreado)
                );
    }

}
