package com.webflux.demo.app.models.services;

import com.webflux.demo.app.models.dao.ProductoDao;
import com.webflux.demo.app.models.documents.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService{

    private final ProductoDao productoDao;

    @Override
    public Flux<Producto> findAll() {
        return productoDao.findAll();
    }

    @Override
    public Mono<Producto> findById(String id) {
        return productoDao.findById(id);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return productoDao.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return productoDao.delete(producto);
    }
}
