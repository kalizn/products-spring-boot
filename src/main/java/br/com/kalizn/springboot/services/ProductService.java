package br.com.kalizn.springboot.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import br.com.kalizn.springboot.controllers.ProductController;
import br.com.kalizn.springboot.dto.ProductRecordDto;
import br.com.kalizn.springboot.models.ProductModel;
import br.com.kalizn.springboot.models.ProductStatus;
import br.com.kalizn.springboot.models.ProductStatusUpdateBody;
import br.com.kalizn.springboot.repositories.ProductRepository;
import br.com.kalizn.springboot.utils.BadRequestException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ProductService {

    @Autowired
	private ProductRepository productRepository;

    // GET All PRODUCTS
    public ResponseEntity<List<ProductModel>> getAllProducts(){
		List<ProductModel> productsList = productRepository.findAll();
		if(!productsList.isEmpty()) {
			for(ProductModel product : productsList) {
				UUID id = product.getIdProduct();
				product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(productsList);
	}

    // GET ONE PRODUCT BY ID
    public ResponseEntity<Object> getOneProduct(UUID id){
		Optional<ProductModel> productO = productRepository.findById(id);
		if(productO.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
		}
		productO.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Products List"));
		return ResponseEntity.status(HttpStatus.OK).body(productO.get());
	}

    // SAVE A NEW PRODUCT
    public ResponseEntity<ProductModel> saveProduct(ProductRecordDto productRecordDto) {
		var productModel = new ProductModel();
		BeanUtils.copyProperties(productRecordDto, productModel);
		return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
	}

    // DELETE A PRODUCT
    public ResponseEntity<Object> deleteProduct(UUID id) {
		Optional<ProductModel> productO = productRepository.findById(id);
		if(productO.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
		}
		productRepository.delete(productO.get());
		return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully.");
	}

    // UPDATE A PRODUCT
    public ResponseEntity<Object> updateProduct(UUID id, ProductRecordDto productRecordDto) {
		Optional<ProductModel> productO = productRepository.findById(id);
		if(productO.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
		}
		var productModel = productO.get();
		BeanUtils.copyProperties(productRecordDto, productModel);
		return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
	}

    // UPDATE A PRODUCT'S STATUS
    public ResponseEntity<Object> updateStatus(UUID id, ProductStatusUpdateBody newStatus) {
        Optional<ProductModel> productO = productRepository.findById(id);

		if(productO.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
		}

        ProductModel product = productO.get();

        if (product.getStatus().equals(ProductStatus.ACTIVE)) {
            throw new BadRequestException("Can't change status to INACTIVE");
        }

        if (product.getStatus().equals(ProductStatus.INACTVE)) {
            throw new BadRequestException("Can't change status ACTIVE");
        }

        product.setStatus(newStatus.getStatus());

        return ResponseEntity.status(HttpStatus.OK).body("Product status updated successfully.");
    }
}
