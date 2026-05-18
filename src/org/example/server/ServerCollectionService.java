package org.example.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.data.City;
import org.example.data.StandardOfLiving;
import org.example.db.CityRepository;
import org.example.validate.CityValidator;
import org.example.validate.InputValidator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class ServerCollectionService {

    private static final Logger log = LogManager.getLogger(ServerCollectionService.class);

    private final CityRepository cityRepository;
    private final List<City> cities = new ArrayList<>();
    private final ReentrantLock collectionLock = new ReentrantLock();

    public ServerCollectionService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public void loadFromDatabase() throws Exception {
        log.info("Загрузка коллекции из БД: проверка уникальности id");
        InputValidator.validateUniqueIds(cityRepository);
        List<City> loaded = cityRepository.loadAllOrderedByStack();
        collectionLock.lock();
        try {
            cities.clear();
            cities.addAll(loaded);
        } finally {
            collectionLock.unlock();
        }
        log.info("Коллекция загружена из PostgreSQL, элементов: {}", loaded.size());
    }

    public String info() {
        collectionLock.lock();
        try {
            return "Type: List (append-only order), persisted in PostgreSQL, size: " + cities.size();
        } finally {
            collectionLock.unlock();
        }
    }

    public int collectionSize() {
        collectionLock.lock();
        try {
            return cities.size();
        } finally {
            collectionLock.unlock();
        }
    }

    public List<City> getSortedByName() {
        collectionLock.lock();
        try {
            List<City> sorted = new ArrayList<>(cities);
            sorted.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            return sorted;
        } finally {
            collectionLock.unlock();
        }
    }

    public City add(City city, long ownerUserId, String ownerLogin) throws Exception {
        city.setId(cityRepository.findFirstAvailableId());
        city.setCreationDate(new Date());
        CityValidator.validateCity(city);

        collectionLock.lock();
        try {
            long newId = cityRepository.insertCity(city, ownerUserId);
            city.setId(newId);
            city.setOwnerUserId(ownerUserId);
            city.setOwnerLogin(ownerLogin);
            cities.add(city);
            return city;
        } finally {
            collectionLock.unlock();
        }
    }

    public boolean addIfMax(City city, long ownerUserId, String ownerLogin) throws Exception {
        city.setId(cityRepository.findFirstAvailableId());
        city.setCreationDate(new Date());
        CityValidator.validateCity(city);

        collectionLock.lock();
        try {
            long maxId = 0;
            for (City current : cities) {
                if (current.getId() != null && current.getId() > maxId) {
                    maxId = current.getId();
                }
            }
            if (city.getId() <= maxId) {
                return false;
            }
            long newId = cityRepository.insertCity(city, ownerUserId);
            city.setId(newId);
            city.setOwnerUserId(ownerUserId);
            city.setOwnerLogin(ownerLogin);
            cities.add(city);
            return true;
        } finally {
            collectionLock.unlock();
        }
    }

    public boolean insertAt(int index, City city, long ownerUserId, String ownerLogin) throws Exception {
        city.setId(cityRepository.findFirstAvailableId());
        city.setCreationDate(new Date());
        CityValidator.validateCity(city);

        collectionLock.lock();
        try {
            // Collection is append-only now: index is accepted for compatibility but ignored.
            long newId = cityRepository.insertCity(city, ownerUserId);
            city.setId(newId);
            city.setOwnerUserId(ownerUserId);
            city.setOwnerLogin(ownerLogin);
            cities.add(city);
            return true;
        } finally {
            collectionLock.unlock();
        }
    }

    public boolean removeById(long id, long ownerUserId) throws Exception {
        collectionLock.lock();
        try {
            if (!cityRepository.deleteByIdAndOwner(id, ownerUserId)) {
                return false;
            }
            cities.removeIf(c -> c.getId() != null && c.getId().equals(id));
            return true;
        } finally {
            collectionLock.unlock();
        }
    }

    public boolean update(long id, City patch, long ownerUserId) throws Exception {
        collectionLock.lock();
        try {
            City target = null;
            for (City city : cities) {
                if (city.getId() != null && city.getId().equals(id)) {
                    target = city;
                    break;
                }
            }
            if (target == null) {
                return false;
            }
            if (!Objects.equals(target.getOwnerUserId(), ownerUserId)) {
                return false;
            }

            patch.setId(target.getId());
            patch.setCreationDate(target.getCreationDate());
            patch.setOwnerUserId(target.getOwnerUserId());
            patch.setOwnerLogin(target.getOwnerLogin());
            CityValidator.validateCity(patch);

            if (!cityRepository.updateByIdAndOwner(id, ownerUserId, patch)) {
                return false;
            }
            target.setName(patch.getName());
            target.setCoordinates(patch.getCoordinates());
            target.setArea(patch.getArea());
            target.setPopulation(patch.getPopulation());
            target.setMetersAboveSeaLevel(patch.getMetersAboveSeaLevel());
            target.setClimate(patch.getClimate());
            target.setGovernment(patch.getGovernment());
            target.setStandardOfLiving(patch.getStandardOfLiving());
            target.setGovernor(patch.getGovernor());
            return true;
        } finally {
            collectionLock.unlock();
        }
    }

    public boolean canUpdate(long id, long ownerUserId) {
        collectionLock.lock();
        try {
            for (City city : cities) {
                if (city.getId() != null && city.getId().equals(id)) {
                    return Objects.equals(city.getOwnerUserId(), ownerUserId);
                }
            }
            return false;
        } finally {
            collectionLock.unlock();
        }
    }

    public void clearForUser(long ownerUserId) throws Exception {
        collectionLock.lock();
        try {
            cityRepository.deleteAllByOwner(ownerUserId);
            cities.removeIf(c -> Objects.equals(ownerUserId, c.getOwnerUserId()));
        } finally {
            collectionLock.unlock();
        }
    }

    public long countLessThan(StandardOfLiving value) {
        collectionLock.lock();
        try {
            long count = 0;
            for (City city : cities) {
                StandardOfLiving cityValue = city.getStandardOfLiving();
                if (cityValue != null && cityValue.getRank() > value.getRank()) {
                    count++;
                }
            }
            return count;
        } finally {
            collectionLock.unlock();
        }
    }

    public List<City> filterByGovernor(String governorText) {
        collectionLock.lock();
        try {
            List<City> filtered = new ArrayList<>();
            for (City city : cities) {
                if (city.getGovernor() == null) {
                    continue;
                }
                if (city.getGovernor().toString().contains(governorText)) {
                    filtered.add(city);
                }
            }
            return filtered;
        } finally {
            collectionLock.unlock();
        }
    }

    public List<StandardOfLiving> getStandardsAscending() {
        collectionLock.lock();
        try {
            List<StandardOfLiving> values = new ArrayList<>();
            for (City city : cities) {
                if (city.getStandardOfLiving() != null) {
                    values.add(city.getStandardOfLiving());
                }
            }
            values.sort((a, b) -> Integer.compare(b.getRank(), a.getRank()));
            return values;
        } finally {
            collectionLock.unlock();
        }
    }
}
