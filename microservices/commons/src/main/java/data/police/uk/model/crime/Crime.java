package data.police.uk.model.crime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Crime {

    private Long id;
    private String category;
    @JsonProperty("persistent_id")
    private String persistentId;
    private String month;
    @JsonProperty("location_type")
    private String locationType;
    @JsonProperty("location_subtype")
    private String locationSubtype;
    private Location location;
    private String context;
    private OutcomeStatus outcomeStatus;
    private String neighbourhood;
    private Long offsetId;

}