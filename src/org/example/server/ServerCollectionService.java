package org.example.server;

import org.example.data.City;
import org.example.data.StandardOfLiving;
import org.example.db.CityRepository;
import org.example.validate.CityValidator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;

public class ServerCollectionService {

    private final CityRepository cityRepository;
    private final Stack<City> cities = new Stack<>();
    private final ReentrantLock collectionLock = new ReentrantLock();

    public ServerCollectionService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public void loadFromDatabase() throws Exception {
        List<City> loaded = cityRepository.loadAllOrderedByStack();
        collectionLock.lock();
        try {
            cities.clear();
            for (City c : loaded) {
                cities.push(c);
            }
        } finally {
            collectionLock.unlock();
        }
    }

    public String info() {
        collectionLock.lock();
        try {
            return "Type: Stack (in memory), persisted in PostgreSQL, size: " + cities.size();
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
        city.setId(null);
        city.setCreationDate(new Date());
        CityValidator.validateCity(city);

        collectionLock.lock();
        try {
            int stackOrder = cities.size();
            long newId = cityRepository.insertCity(city, ownerUserId, stackOrder);
            city.setId(newId);
            city.setOwnerUserId(ownerUserId);
            city.setOwnerLogin(ownerLogin);
            cities.push(city);
            return city;
        } finally {
            collectionLock.unlock();
        }
    }

    public boolean addIfMax(City city, long ownerUserId, String ownerLogin) throws Exception {
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
            if (city.getId() != null && city.getId() <= maxId) {
                return false;
            }
            city.setId(null);
            int stackOrder = cities.size();
            long newId = cityRepository.insertCity(city, ownerUserId, stackOrder);
            city.setId(newId);
            city.setOwnerUserId(ownerUserId);
            city.setOwnerLogin(ownerLogin);
            cities.push(city);
            return true;
        } finally {
            collectionLock.unlock();
        }
    }

    public boolean insertAt(int index, City city, long ownerUserId, String ownerLogin) throws Exception {
        city.setId(null);
        city.setCreationDate(new Date());
        CityValidator.validateCity(city);

        collectionLock.lock();
        try {
            if (index < 0 || index > cities.size()) {
                return false;
            }
            long newId = cityRepository.insertCity(city, ownerUserId, index);
            city.setId(newId);
            city.setOwnerUserId(ownerUserId);
            city.setOwnerLogin(ownerLogin);
            cities.add(index, city);
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
        CityValidator.validateCity(patch);
        collectionLock.lock();
        try {
            if (!cityRepository.updateByIdAndOwner(id, ownerUserId, patch)) {
                return false;
            }
            for (City target : cities) {
                if (target.getId() != null && target.getId().equals(id)) {
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
