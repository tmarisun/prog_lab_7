package org.example.data;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.io.Serializable;

/**
 * Представляет город с фиксированным набором характеристик.
 * Сравнивается по {@code id} (реализует {@link Comparable}), равенство определяется только по {@code id}.
 * @see Coordinates
 * @see Human
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString

public class City implements Comparable<City>, Serializable {

    private Long id;
    private String name;
    private Coordinates coordinates;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.util.Date creationDate;
    private Double area;
    private int population;
    private int metersAboveSeaLevel;
    private Climate climate;
    private Government government;
    private StandardOfLiving standardOfLiving;
    private Human governor;

    /** Владелец записи */
    private Long ownerUserId;
    private String ownerLogin;


    @Override
    public int compareTo(City other) {
        if (other == null) return 1;
        return this.id.compareTo(other.id);
    }
}