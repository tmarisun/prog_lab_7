package org.example.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.data.City;
import org.example.data.Climate;
import org.example.data.Coordinates;
import org.example.data.Government;
import org.example.data.Human;
import org.example.data.StandardOfLiving;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CityRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private final Database database;

    public CityRepository(Database database) {
        this.database = database;
    }

    public List<City> loadAllOrderedByStack() throws Exception {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT c.id, c.stack_order, c.name, c.coordinates_json, c.creation_date, c.area,
                            c.population, c.meters_above_sea_level, c.climate, c.government,
                            c.standard_of_living, c.governor_json, c.owner_user_id, u.login AS owner_login
                     FROM cities c
                     JOIN users u ON u.id = c.owner_user_id
                     ORDER BY c.stack_order ASC
                     """)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<City> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }
        }
    }

    public long insertCity(City city, long ownerUserId) throws Exception {
        try (Connection c = database.getConnection()) {
            c.setAutoCommit(false);
            try {
                long generatedId = findFirstAvailableIdOnConnection(c);
                city.setId(generatedId);
                int nextStackOrder = findNextStackOrderOnConnection(c);
                long id = insertRow(c, city, ownerUserId, nextStackOrder);
                c.commit();
                return id;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public long findFirstAvailableId() throws Exception {
        try (Connection c = database.getConnection()) {
            return findFirstAvailableIdOnConnection(c);
        }
    }

    public void validateUniqueIdsInDatabase() throws Exception {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT id
                     FROM cities
                     GROUP BY id
                     HAVING COUNT(*) > 1
                     LIMIT 1
                     """);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                throw new IllegalArgumentException("Duplicate ID found in database: " + rs.getLong(1));
            }
        }
    }

    public boolean deleteByIdAndOwner(long cityId, long ownerUserId) throws Exception {
        try (Connection c = database.getConnection();
             PreparedStatement del = c.prepareStatement(
                     "DELETE FROM cities WHERE id = ? AND owner_user_id = ?")) {
            del.setLong(1, cityId);
            del.setLong(2, ownerUserId);
            return del.executeUpdate() == 1;
        }
    }

    public boolean updateByIdAndOwner(long cityId, long ownerUserId, City patch) throws Exception {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                     UPDATE cities SET
                         name = ?,
                         coordinates_json = ?,
                         area = ?,
                         population = ?,
                         meters_above_sea_level = ?,
                         climate = ?,
                         government = ?,
                         standard_of_living = ?,
                         governor_json = ?
                     WHERE id = ? AND owner_user_id = ?
                     """)) {
            ps.setString(1, patch.getName());
            ps.setString(2, MAPPER.writeValueAsString(patch.getCoordinates()));
            ps.setDouble(3, patch.getArea());
            ps.setInt(4, patch.getPopulation());
            ps.setInt(5, patch.getMetersAboveSeaLevel());
            if (patch.getClimate() == null) {
                ps.setString(6, null);
            } else {
                ps.setString(6, patch.getClimate().name());
            }
            ps.setString(7, patch.getGovernment().name());
            if (patch.getStandardOfLiving() == null) {
                ps.setString(8, null);
            } else {
                ps.setString(8, patch.getStandardOfLiving().name());
            }
            ps.setString(9, governorJson(patch.getGovernor()));
            ps.setLong(10, cityId);
            ps.setLong(11, ownerUserId);
            return ps.executeUpdate() == 1;
        }
    }

    public int deleteAllByOwner(long ownerUserId) throws Exception {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM cities WHERE owner_user_id = ?")) {
            ps.setLong(1, ownerUserId);
            return ps.executeUpdate();
        }
    }

    private static int findNextStackOrderOnConnection(Connection c) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(MAX(stack_order), -1) + 1 FROM cities");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new IllegalStateException("Failed to compute next stack_order");
    }

    private static long findFirstAvailableIdOnConnection(Connection c) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT MIN(gs) AS next_id
                FROM generate_series(
                    1,
                    COALESCE((SELECT MAX(id) FROM cities), 0) + 1
                ) gs
                LEFT JOIN cities c ON c.id = gs
                WHERE c.id IS NULL
                """);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("next_id");
            }
        }
        throw new IllegalStateException("Failed to generate next available city id");
    }

    private static long insertRow(Connection c, City city, long ownerUserId, int stackOrder) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO cities (
                    id, stack_order, name, coordinates_json, creation_date, area, population,
                    meters_above_sea_level, climate, government, standard_of_living, governor_json, owner_user_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """)) {
            ps.setLong(1, city.getId());
            ps.setInt(2, stackOrder);
            ps.setString(3, city.getName());
            ps.setString(4, MAPPER.writeValueAsString(city.getCoordinates()));
            ps.setTimestamp(5, new Timestamp(city.getCreationDate().getTime()));
            ps.setDouble(6, city.getArea());
            ps.setInt(7, city.getPopulation());
            ps.setInt(8, city.getMetersAboveSeaLevel());
            if (city.getClimate() == null) {
                ps.setString(9, null);
            } else {
                ps.setString(9, city.getClimate().name());
            }
            ps.setString(10, city.getGovernment().name());
            if (city.getStandardOfLiving() == null) {
                ps.setString(11, null);
            } else {
                ps.setString(11, city.getStandardOfLiving().name());
            }
            ps.setString(12, governorJson(city.getGovernor()));
            ps.setLong(13, ownerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new IllegalStateException("INSERT did not return id");
    }

    private static City mapRow(ResultSet rs) throws Exception {
        City city = new City();
        city.setId(rs.getLong("id"));
        city.setName(rs.getString("name"));
        city.setCoordinates(MAPPER.readValue(rs.getString("coordinates_json"), Coordinates.class));
        city.setCreationDate(rs.getTimestamp("creation_date"));
        city.setArea(rs.getDouble("area"));
        city.setPopulation(rs.getInt("population"));
        city.setMetersAboveSeaLevel(rs.getInt("meters_above_sea_level"));
        String climate = rs.getString("climate");
        city.setClimate(parseClimate(rs.getString("climate")));
        city.setGovernment(Government.valueOf(rs.getString("government")));
        city.setStandardOfLiving(parseStandardOfLiving(rs.getString("standard_of_living")));
        city.setGovernor(parseGovernor(rs.getString("governor_json")));
        city.setOwnerUserId(rs.getLong("owner_user_id"));
        city.setOwnerLogin(rs.getString("owner_login"));
        return city;
    }

    private static String governorJson(Human governor) throws Exception {
        if (governor == null) {
            return null;
        }
        return MAPPER.writeValueAsString(governor);
    }

    private static Climate parseClimate(String name) {
        if (name == null) {
            return null;
        }
        return Climate.valueOf(name);
    }

    private static StandardOfLiving parseStandardOfLiving(String name) {
        if (name == null) {
            return null;
        }
        return StandardOfLiving.valueOf(name);
    }

    private static Human parseGovernor(String govJson) throws Exception {
        if (govJson == null) {
            return null;
        }
        return MAPPER.readValue(govJson, Human.class);
    }
}
