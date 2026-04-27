package it.warehouse.transport.dto.movementhistory;

import it.warehouse.transport.model.MovementStatusHistory;
import it.warehouse.transport.model.MovementTrack;
import it.warehouse.transport.model.Product;
import it.warehouse.transport.model.enumerator.MovementStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class InsertMovementStatusHistoryDTO {

    @NotNull(message = "Movement track ID must be valorized")
    private UUID movementTrackId;

    @NotNull(message = "Product ID must be valorized")
    private UUID productID;

    @NotNull(message = "Quantity must be valorized")
    @Positive(message = "Quantity must be a positive value")
    private Integer quantity;

    private String notes;


    public MovementStatusHistory toEntity(){
        MovementStatusHistory msh = new MovementStatusHistory();
        msh.setMovementTrack(msh.db().reference(MovementTrack.class,movementTrackId));
        msh.setProduct(msh.db().reference(Product.class,productID));
        msh.setQuantity(quantity);
        msh.setNotes(notes);
        return msh;
    }

}
