package com.webflux.demo.app.controllers;

import com.webflux.demo.app.models.documents.Producto;
import com.webflux.demo.app.models.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    public static final String API_PRODUCTOS_BASIC_URI = "/api/productos/";
    private final ProductoService productoService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping("/basicfindall")
    public Flux<Producto> listar() {
        return productoService.findAll();
    }

    @GetMapping("/responseentityfindall")
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


}
