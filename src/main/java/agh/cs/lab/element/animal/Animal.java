package agh.cs.lab.element.animal;

import agh.cs.lab.element.Entity;
import agh.cs.lab.shared.Vector2d;
import agh.cs.lab.shared.MapDirection;

import java.util.List;
import java.util.Set;

public class Animal extends Entity {

    private final List<Integer> genes;
    private final Set<AnimalObserver> observers;

    private Vector2d position;
    private MapDirection orientation;
    private int energy;

    Animal(int id, Set<AnimalObserver> observers, Vector2d position, List<Integer> genes, MapDirection orientation,
           int energy) {
        super(id);
        this.genes = genes;
        this.observers = observers;
        this.position = position;
        this.orientation = orientation;
        this.energy = energy;

        observers.forEach(obs -> obs.onAnimalBorn(this));
    }

    public void kill() {
        observers.forEach(obs -> obs.onAnimalDead(this));
    }

    public void turn(MapDirection newOrientation) {
        var prevOrientation = orientation;
        orientation = newOrientation;

        observers.forEach(obs -> obs.onAnimalTurned(this, prevOrientation));
    }

    public void move(Vector2d newPosition) {
        var prevPosition = position;
        position = newPosition;

        observers.forEach(obs -> obs.onAnimalMoved(this, prevPosition));
    }

    public List<Integer> getGenes() {
        return genes;
    }

    public MapDirection getOrientation() {
        return orientation;
    }

    public int getEnergy() {
        return energy;
    }

    public void addEnergy(int energy) {
        this.energy += energy;
        observers.forEach(obs -> obs.onEnergyChanged(this, this.energy - energy));
    }

    public void subtractEnergy(int energy) {
        this.energy -= energy;
        observers.forEach(obs -> obs.onEnergyChanged(this, this.energy + energy));
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }
}