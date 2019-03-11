package model;

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
