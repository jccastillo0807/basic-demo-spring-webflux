package com.webflux.demo.app.models.services;

import com.webflux.demo.app.models.documents.Categoria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoriaService {

    public Flux<Categoria> findAllCategoria();


    public Mono<Categoria> findCategoriaById(String id);


    public Mono<Categoria> saveCategoria(Categoria categoria);
}
