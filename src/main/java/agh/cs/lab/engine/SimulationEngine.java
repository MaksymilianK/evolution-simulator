package agh.cs.lab.engine;

import agh.cs.lab.element.Entity;
import agh.cs.lab.element.EntityFactoryFacade;
import agh.cs.lab.element.animal.Animal;
import agh.cs.lab.element.animal.Gene;
import agh.cs.lab.element.plant.Plant;
import agh.cs.lab.engine.map.WorldMap;
import agh.cs.lab.shared.*;

import java.util.*;

public class SimulationEngine {

    private final SimulationSettings settings;
    private final WorldMap map;
    private final EntityFactoryFacade entityFactory;
    private final Rand rand;
    private final Set<Animal> animals = new HashSet<>();
    private final Set<Plant> plants = new HashSet<>();

    private int currentEpoch = 0;

    private SimulationEngine(SimulationSettings settings, WorldMap map, EntityFactoryFacade entityFactory, Rand rand) {
        this.settings = settings;
        this.map = map;
        this.entityFactory = entityFactory;
        this.rand = rand;
    }

    public SimulationSettings getSettings() {
        return settings;
    }

    public void nextEpoch() {
        animals.forEach(Entity::addEpochToLife);
        animals.forEach(Entity::addEpochToLife);
        currentEpoch++;
    }

    public int getCurrentEpoch() {
        return currentEpoch;
    }

    public Set<Animal> removeDeadAnimals() {
        var deadAnimals = new HashSet<Animal>();

        var iterator = animals.iterator();
        while (iterator.hasNext()) {
            var animal = iterator.next();
            if (animal.getEnergy() < settings.getMoveEnergy()) {
                animal.kill();
                deadAnimals.add(animal);
                iterator.remove();
            }
        }

        return deadAnimals;
    }

    public void turnAnimals() {
        animals.forEach(animal -> animal.turn(getNewOrientation(animal)));
    }

    public void moveAnimals() {
        animals.forEach(animal -> {
            animal.move(map.getNewPosition(animal.getPosition(), animal.getOrientation()));
            animal.subtractEnergy(settings.getMoveEnergy());
        });
    }

    public Collection<Plant> feedAnimals() {
        var eatenPlants = new HashSet<Plant>();

        map.getAnimalsToFeed().forEach(field -> {
            field.first.eat();
            eatenPlants.add(field.first);
            field.second.forEach(animal -> animal.addEnergy(settings.getPlantEnergy() / field.second.size()));

            plants.remove(field.first);
        });

        return eatenPlants;
    }

    public Set<Trio<Animal, Animal, Animal>> procreate() {
        var newChildren = new HashSet<Trio<Animal, Animal, Animal>>();

        map.getAnimalsToProcreate(settings.getProcreationEnergy()).forEach(animals -> {
            Animal parent1 = null;
            Animal parent2 = null;
            if (animals.get(0).getEnergy() == animals.get(1).getEnergy()) {
                parent1 = animals.get(rand.randInt(animals.size()));
                animals.remove(parent1);
                parent2 = animals.get(rand.randInt(0, animals.size()));
            } else {
                parent1 = animals.get(0);
                parent2 = animals.get(rand.randInt(1, animals.size()));
            }

            Vector2d childPosition;
            var adjacentFields = map.getEmptyAdjacentFields(parent1.getPosition());

            if (adjacentFields.isEmpty()) {
                childPosition = map.getNewPosition(parent1.getPosition(), Direction.values()[rand.randInt(8)]);
            } else {
                childPosition = adjacentFields.get(rand.randInt(adjacentFields.size()));
            }

            var child = entityFactory.giveBirthToAnimal(childPosition, parent1, parent2);

            parent1.subtractEnergy(parent1.getEnergy() / 4);
            parent2.subtractEnergy(parent2.getEnergy() / 4);

            this.animals.add(child);
            newChildren.add(new Trio<>(child, parent1, parent2));
        });

        return newChildren;
    }

    public Collection<Plant> addPlants() {
        var newPlants = new ArrayList<Plant>(2);

        var plant1 = trySetPlant(map.getEmptyFieldsInsideJungle());
        var plant2 = trySetPlant(map.getEmptyFieldsOutsideJungle());

        if (plant1 != null) {
            newPlants.add(plant1);
        }
        if (plant2 != null) {
            newPlants.add(plant2);
        }

        return newPlants;
    }

    public Set<Animal> getAnimals() {
        return Collections.unmodifiableSet(animals);
    }

    public Set<Animal> getAnimalsWithTopEnergy() {
        return map.getAnimalsWithTopEnergy();
    }

    public Set<Plant> getPlants() {
        return map.getPlants();
    }

    public Pair<Vector2d, Vector2d> getMapBorders() {
        return map.getMapBorders();
    }

    public Pair<Vector2d, Vector2d> getMapJungleBorders() {
        return map.getJungleBorders();
    }

    public Optional<Animal> getHealthiestAnimalAt(Vector2d position) {
        return map.getHealthiestAnimalAt(position);
    }

    public Set<Animal> getAnimalsWithGenes(Collection<Gene> genes) {
        return map.getHealthiestAnimalsWithGenes(genes);
    }

    private Plant trySetPlant(List<Vector2d> fields) {
        if (fields.isEmpty()) {
            return null;
        }

        int index = rand.randInt(fields.size());
        var plant = entityFactory.createPlant(fields.get(index));
        plants.add(plant);
        return plant;
    }

    private Direction getNewOrientation(Animal animal) {
        int turn = rand.randInt(8);
        var newOrientation = animal.getOrientation();

        for (int i = 0; i < turn; i++) {
            newOrientation = newOrientation.next();
        }
        return newOrientation;
    }

    public static SimulationEngine create(SimulationSettings settings, WorldMap map, EntityFactoryFacade entityFactory,
                                          Rand rand, int startAnimals) {
        var emptyFields = map.getEmptyFieldsInsideJungle();
        emptyFields.addAll(map.getEmptyFieldsOutsideJungle());

        if (emptyFields.size() < startAnimals) {
            throw new SimulationException("Cannot create so many animals on simulation start - there is too few fields " +
                    "available");
        }

        var engine = new SimulationEngine(settings, map, entityFactory, rand);

        for (int i = 0; i < startAnimals; i++) {
            int index = rand.randInt(emptyFields.size());
            engine.animals.add(entityFactory.createAnimal(emptyFields.get(index)));
            emptyFields.remove(index);
        }

        return engine;
    }
}
