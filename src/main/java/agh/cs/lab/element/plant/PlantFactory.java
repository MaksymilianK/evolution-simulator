package agh.cs.lab.element.plant;

import agh.cs.lab.element.EntityFactory;
import agh.cs.lab.statistics.SimulationStatisticsManager;
import agh.cs.lab.engine.map.WorldMap;
import agh.cs.lab.shared.Vector2d;

import java.util.Set;

public class PlantFactory extends EntityFactory {

    private int counter = 0;

    public PlantFactory(WorldMap map, SimulationStatisticsManager statistics) {
        super(map, statistics);
    }

    public Plant create(Vector2d position) {
        return Plant.create(++counter, position, Set.of(map, statistics));
    }
}
