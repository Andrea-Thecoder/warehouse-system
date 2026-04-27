package it.warehouse.transport.dto.stock;

import it.warehouse.transport.dto.product.SimpleDetailProductDTO;
import it.warehouse.transport.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.transport.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public final class DetailStockDTO extends  BaseDetailStockDTO {

    private SimpleDetailWarehouseDTO warehouse;


    public static DetailStockDTO of (Stock stock){
        DetailStockDTO dto = new DetailStockDTO();
        dto.setId(stock.getId());
        dto.setProduct(SimpleDetailProductDTO.of(stock.getProduct()));
        dto.setWarehouse(SimpleDetailWarehouseDTO.of(stock.getWarehouse()));
        dto.setQuantity(stock.getQuantity());
        return dto;
    }
}
