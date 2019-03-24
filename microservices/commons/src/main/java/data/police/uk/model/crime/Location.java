package data.police.uk.model.crime;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Location {

    private BigDecimal longitude;
    private BigDecimal latitude;
    private Street street;

}
