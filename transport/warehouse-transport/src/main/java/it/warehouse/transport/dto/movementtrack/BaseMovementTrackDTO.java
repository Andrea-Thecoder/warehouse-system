package it.warehouse.transport.dto.movementtrack;

import it.warehouse.transport.dto.product.SimpleDetailProductDTO;
import it.warehouse.transport.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.transport.model.MovementTrack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public sealed class BaseMovementTrackDTO permits DetailMovementTrackDTO, BaseDetailMovementTrackDTO {

    protected UUID id;
    protected SimpleDetailWarehouseDTO originWarehouse;
    protected SimpleDetailProductDTO product;
    protected Integer quantity;
    protected LocalDateTime estimatedArrival;


}
