package it.warehouse.transport.dto.stock;

import it.warehouse.transport.dto.product.SimpleDetailProductDTO;
import it.warehouse.transport.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.transport.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public sealed class BaseDetailStockDTO permits  DetailStockDTO, DetailStockWarehouseDTO {

    protected Long id;
    protected SimpleDetailProductDTO product;
    protected int quantity;



}
