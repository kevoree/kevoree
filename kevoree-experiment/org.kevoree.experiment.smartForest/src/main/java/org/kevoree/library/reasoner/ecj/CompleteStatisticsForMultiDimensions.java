package org.kevoree.library.reasoner.ecj;

import ec.EvolutionState;
import ec.Statistics;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jbourcie
 * Date: 07/07/11
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
public class CompleteStatisticsForMultiDimensions extends Statistics {

    public static final String P_STATISTICS_FILE = "file";
    public static final String P_WRITE_MODELS = "write-models";

    public int statisticsLog;
    public boolean writeModels = false;

    // timings
    public long lastTime;

    public CompleteStatisticsForMultiDimensions() {
        statisticsLog = 0; /* stdout */
    }

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        File statisticsFile = state.parameters.getFile(
                base.push(P_STATISTICS_FILE), null);

        if (statisticsFile != null) try {
            statisticsLog = state.output.addLog(statisticsFile, false);
        } catch (IOException i) {
            state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
        }
        writeModels = state.parameters.getBoolean(base.push(P_WRITE_MODELS),null,false);
    }

    public void preInitializationStatistics(final EvolutionState state) {
        super.preInitializationStatistics(state);

        lastTime = System.currentTimeMillis();
    }

    public void postInitializationStatistics(final EvolutionState state) {
        long interval = System.currentTimeMillis() - lastTime;
        super.postInitializationStatistics(state);
        state.output.print("0;", statisticsLog);
        state.output.print("" + interval + ";", statisticsLog);
    }

    public void preBreedingStatistics(final EvolutionState state) {
        super.preBreedingStatistics(state);
        lastTime = System.currentTimeMillis();
    }

    public void postBreedingStatistics(final EvolutionState state) {
        long interval = System.currentTimeMillis() - lastTime;
        super.postBreedingStatistics(state);
        state.output.print("" + (state.generation + 1) + ";", statisticsLog); // 1 because we're putting the breeding info on the same line as the generation it *produces*, and the generation number is increased *after* breeding occurs, and statistics for it

        // gather timings
        state.output.print("" + interval + ";", statisticsLog);
    }

    public void preEvaluationStatistics(final EvolutionState state) {
        super.preEvaluationStatistics(state);
        lastTime = System.currentTimeMillis();
    }


    /**
     * Prints out the statistics, but does not end with a println --
     * this lets overriding methods print additional statistics on the same line
     */
    protected void _postEvaluationStatistics(final EvolutionState state) {

        for (int x = 0; x < state.population.subpops.length; x++) {
            for (int y = 0; y < state.population.subpops[x].individuals.length; y++) {
                if (state.population.subpops[x].individuals[y].evaluated)        // he's got a valid fitness
                {
                    if (writeModels)
                        state.output.print(state.population.subpops[x].individuals[y].toString() + ";", statisticsLog);
                    if (state.population.subpops[x].individuals[y].fitness instanceof MultiObjectiveFitness)
                        state.output.print(((MultiObjectiveFitness) state.population.subpops[x].individuals[y].fitness).fitnessToStringForHumans() + "*", statisticsLog);
                }
            }
        }
    }


    public void postEvaluationStatistics(final EvolutionState state) {
        long interval = System.currentTimeMillis() - lastTime;
        super.postEvaluationStatistics(state);
        state.output.print(interval + "#", statisticsLog);

        _postEvaluationStatistics(state);
        state.output.println("", statisticsLog);
    }
}
