package it.warehouse.transport.dto.product;

import it.warehouse.transport.model.CategoryType;
import it.warehouse.transport.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SimpleDetailProductDTO {

    private UUID id;
    private String name;
    private CategoryType category;
    
    public static SimpleDetailProductDTO of (Product product){
        SimpleDetailProductDTO dto = new SimpleDetailProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        return dto;
    }
}
