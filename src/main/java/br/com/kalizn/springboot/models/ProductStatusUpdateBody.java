package br.com.kalizn.springboot.models;

public class ProductStatusUpdateBody {
    
    private ProductStatus status;

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}
