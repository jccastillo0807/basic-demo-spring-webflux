package com.webflux.demo.app;

import com.webflux.demo.app.models.documents.Categoria;
import com.webflux.demo.app.models.documents.Producto;
import com.webflux.demo.app.models.services.CategoriaService;
import com.webflux.demo.app.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class DemoWebfluxApplication implements CommandLineRunner {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;
    private static final Logger log = LoggerFactory.getLogger(DemoWebfluxApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        mongoTemplate.dropCollection("productos").subscribe();
        mongoTemplate.dropCollection("categorias").subscribe();

        Categoria electronico = new Categoria("Electrónico");
        Categoria deporte = new Categoria("Deporte");
        Categoria computacion = new Categoria("Computación");
        Categoria Eletrodomesticos = new Categoria("Eletrodomesticos");
        Categoria muebles = new Categoria("Muebles");

        Flux.just(electronico, deporte, computacion, muebles, Eletrodomesticos)
                .flatMap(categoria -> categoriaService.saveCategoria(categoria))
                .doOnNext(categoriaGuardada -> {
                    log.info("Categoria creada: " + categoriaGuardada.getNombre() + ", Id: " + categoriaGuardada.getId());
                })
                .thenMany(
                        Flux.just(
                                        new Producto("TV Panasonic Pantalla LCD", 456.89),
                                        new Producto("Camara Sony HD Digital", 200.14),
                                        new Producto("Ipad Apple", 47.5),
                                        new Producto("Notebook Samsung", 78.6),
                                        new Producto("Portatil ASUS", 2789.6),
                                        new Producto("Mica cómoda", 158.96),
                                        new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
                                        new Producto("Sony Camara HD Digital", 177.89, electronico),
                                        new Producto("Apple iPod", 46.89, electronico),
                                        new Producto("Sony Notebook", 846.89, computacion),
                                        new Producto("Hewlett Packard Multifuncional", 200.89, computacion),
                                        new Producto("Bianchi Bicicleta", 70.89, deporte),
                                        new Producto("HP Notebook Omen 17", 2500.89, computacion),
                                        new Producto("Mica Cómoda 5 Cajones", 150.89, muebles),
                                        new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico)
                                )
                                .flatMap(producto -> {
                                    producto.setCreateAt(new Date());
                                    return productoService.save(producto);
                                })
                )
                .subscribe(producto -> log.info("Insert: " + producto.getId() + producto.getNombre()));
    }
}
