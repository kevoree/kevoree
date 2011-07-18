package org.kevoree.library.reasoner.ecj;

import java.io.File;
import java.io.PrintWriter;

import org.kevoree.tools.marShell.parser.ParserUtil;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.multiobjective.MultiObjectiveStatistics;
import ec.simple.SimpleStatistics;
import ec.util.Checkpoint;
import ec.util.MersenneTwisterFast;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.util.Version;

public class KevoreeGeneticAlgorithm {
    public final static String P_PRINTACCESSEDPARAMETERS = "print-accessed-params";
    public final static String P_PRINTUSEDPARAMETERS = "print-used-params";
    public final static String P_PRINTALLPARAMETERS = "print-all-params";
    public final static String P_PRINTUNUSEDPARAMETERS = "print-unused-params";
    public final static String P_PRINTUNACCESSEDPARAMETERS = "print-unaccessed-params";

    /** The argument indicating that we're starting up from a checkpoint file. */
    public static final String A_CHECKPOINT = "-checkpoint";

    /**
     * The argument indicating that we're starting fresh from a new parameter
     * file.
     */
    public static final String A_FILE = "-file";

    /** evalthreads parameter */
    public static final String P_EVALTHREADS = "evalthreads";

    /** breedthreads parameter */
    public static final String P_BREEDTHREADS = "breedthreads";

    /** seed parameter */
    public static final String P_SEED = "seed";

    /** 'time' seed parameter value */
    public static final String V_SEED_TIME = "time";

    /** state parameter */
    public static final String P_STATE = "state";

    /** 'auto' thread parameter value */
    public static final String V_THREADS_AUTO = "auto";

    /**
     * Restores an EvolutionState from checkpoint if "-checkpoint FILENAME" is
     * in the command-line arguments.
     */
    public static EvolutionState possiblyRestoreFromCheckpoint(String[] args) {
        for (int x = 0; x < args.length - 1; x++)
            if (args[x].equals(A_CHECKPOINT)) {
                System.err.println("Restoring from Checkpoint " + args[x + 1]);
                try {
                    return Checkpoint.restoreFromCheckpoint(args[x + 1]);
                } catch (Exception e) {
                    Output.initialError("An exception was generated upon starting up from a checkpoint.\nHere it is:\n"
                            + e);
                }
            }
        return null; // should never happen
    }

    /**
     * Loads a ParameterDatabase from checkpoint if "-params" is in the
     * command-line arguments.
     */
    public static ParameterDatabase loadParameterDatabase(String[] args) {
        ParameterDatabase parameters = null;
        for (int x = 0; x < args.length - 1; x++)
            if (args[x].equals(A_FILE))
                try {
                    parameters = new ParameterDatabase(new File(new File(
                            args[x + 1]).getAbsolutePath()), args);
                    break;
                } catch (Exception e) {
                    Output.initialError("An exception was generated upon reading the parameter file \""
                            + args[x + 1] + "\".\nHere it is:\n" + e);
                }
        if (parameters == null)
            Output.initialError("No parameter file was specified.");
        return parameters;
    }

    /** Loads the number of threads. */
    public static int determineThreads(Output output,
            ParameterDatabase parameters, Parameter threadParameter) {
        int thread = 1;
        String tmp_s = parameters.getString(threadParameter, null);
        if (tmp_s == null) // uh oh
        {
            output.fatal("Threads number must exist.", threadParameter, null);
        } else if (V_THREADS_AUTO.equalsIgnoreCase(tmp_s)) {
            Runtime runtime = Runtime.getRuntime();
            try {
                return ((Integer) runtime.getClass()
                        .getMethod("availableProcessors", (Class[]) null)
                        .invoke(runtime, (Object[]) null)).intValue();
            } catch (Exception e) {
                output.fatal(
                        "Whoa! This Java version is too old to have the Runtime.availableProcessors() method available.\n"
                                + "This means you can't use 'auto' as a threads option.",
                        threadParameter, null);
            }
        } else {
            try {
                thread = parameters.getInt(threadParameter, null);
            } catch (NumberFormatException e) {
                output.fatal("Invalid, non-integer threads value (" + thread
                        + ")", threadParameter, null);
            }
        }
        return thread;
    }

    /**
     * Primes the generator. Mersenne Twister seeds its first 624 numbers using
     * a basic linear congruential generator; thereafter it uses the
     * MersenneTwister algorithm to build new seeds. Those first 624 numbers are
     * generally just fine, but to be extra safe, you can prime the generator by
     * calling nextInt() on it some (N>1) * 624 times. This method does exactly
     * that, presently with N=2.
     */
    public static MersenneTwisterFast primeGenerator(
            MersenneTwisterFast generator) {
        for (int i = 0; i < 624 * 2; i++)
            generator.nextInt();
        return generator;
    }

    /**
     * Loads a random generator seed. First, the seed is loaded from the
     * seedParameter. If the parameter is V_SEED_TIME, the seed is set to the
     * currentTime value. Then the seed is incremented by the offset. This
     * method is broken out of initialize(...) primarily to share code with
     * ec.eval.MasterProblem.
     */
    public static int determineSeed(Output output,
            ParameterDatabase parameters, Parameter seedParameter,
            long currentTime, int offset, boolean auto) {
        int seed = 1; // have to initialize to make the compiler happy
        String tmp_s = parameters.getString(seedParameter, null);
        if (tmp_s == null && !auto) // uh oh
        {
            output.fatal("Seed must exist.", seedParameter, null);
        } else if (V_SEED_TIME.equalsIgnoreCase(tmp_s)
                || (tmp_s == null && auto)) {
            if (tmp_s == null && auto)
                output.warnOnce("Using automatic determination number of threads, but not all seeds are defined.\nThe rest will be defined using the wall clock time.");
            seed = (int) currentTime; // using low-order bits so it's probably
                                      // okay
            if (seed == 0)
                output.fatal(
                        "Whoa! This Java version is returning 0 for System.currentTimeMillis(), which ain't right.  This means you can't use '"
                                + V_SEED_TIME + "' as a seed ", seedParameter,
                        null);
        } else {
            try {
                seed = parameters.getInt(seedParameter, null);
            } catch (NumberFormatException e) {
                output.fatal("Invalid, non-integer seed value (" + seed + ")",
                        seedParameter, null);
            }
        }
        return seed + offset;
    }

    /**
     * Initializes an evolutionary run given the parameters and a random seed
     * adjustment (added to each random seed). The adjustment offers a
     * convenient way to change the seeds of the random number generators each
     * time you do a new evolutionary run. You are of course welcome to replace
     * the random number generators after initialize(...) but before
     * startFresh(...)
     */

    public static EvolutionState initialize(ParameterDatabase parameters,
            int randomSeedOffset) {
        Output output;
        // 1. create the output

        output = new Output(true);

        // stdout is always log #0. stderr is always log #1.
        // stderr accepts announcements, and both are fully verbose
        // by default.
        output.addLog(ec.util.Log.D_STDOUT, false);
        output.addLog(ec.util.Log.D_STDERR, true);

        // now continue intialization
        return initialize(parameters, randomSeedOffset, output);
    }

    /**
     * Initializes an evolutionary run given the parameters and a random seed
     * adjustment (added to each random seed), with the Output pre-constructed.
     * The adjustment offers a convenient way to change the seeds of the random
     * number generators each time you do a new evolutionary run. You are of
     * course welcome to replace the random number generators after
     * initialize(...) but before startFresh(...)
     */

    public static EvolutionState initialize(ParameterDatabase parameters,
            int randomSeedOffset, Output output) {
        EvolutionState state = null;
        MersenneTwisterFast[] random;
        int[] seeds;
        int breedthreads = 1;
        int evalthreads = 1;
        boolean store;
        int x;

        // output was already created for us.
        output.systemMessage(Version.message());

        // 2. set up thread values

        breedthreads = Evolve.determineThreads(output, parameters,
                new Parameter(P_BREEDTHREADS));
        evalthreads = Evolve.determineThreads(output, parameters,
                new Parameter(P_EVALTHREADS));
        boolean auto = (V_THREADS_AUTO.equalsIgnoreCase(parameters.getString(
                new Parameter(P_BREEDTHREADS), null)) || V_THREADS_AUTO
                .equalsIgnoreCase(parameters.getString(new Parameter(
                        P_EVALTHREADS), null))); // at least one thread is
                                                 // automatic. Seeds may need to
                                                 // be dynamic.

        // 3. create the Mersenne Twister random number generators,
        // one per thread

        random = new MersenneTwisterFast[breedthreads > evalthreads ? breedthreads
                : evalthreads];
        seeds = new int[random.length];

        String seedMessage = "Seed: ";
        int time = (int) (System.currentTimeMillis());
        for (x = 0; x < random.length; x++) {
            seeds[x] = determineSeed(output, parameters,
                    new Parameter(P_SEED).push("" + x), time + x, random.length
                            * randomSeedOffset, auto);
            for (int y = 0; y < x; y++)
                if (seeds[x] == seeds[y])
                    output.fatal(P_SEED + "." + x + " (" + seeds[x] + ") and "
                            + P_SEED + "." + y + " (" + seeds[y]
                            + ") ought not be the same seed.", null, null);
            random[x] = Evolve
                    .primeGenerator(new MersenneTwisterFast(seeds[x])); // we
                                                                        // prime
                                                                        // the
                                                                        // generator
                                                                        // to be
                                                                        // more
                                                                        // sure
                                                                        // of
                                                                        // randomness.
            seedMessage = seedMessage + seeds[x] + " ";
        }

        // 4. Start up the evolution

        // what evolution state to use?
        state = (EvolutionState) parameters.getInstanceForParameter(
                new Parameter(P_STATE), null, EvolutionState.class);
        state.parameters = parameters;
        state.random = random;
        state.output = output;
        state.evalthreads = evalthreads;
        state.breedthreads = breedthreads;
        state.randomSeedOffset = randomSeedOffset;

        output.systemMessage("Threads:  breed/" + breedthreads + " eval/"
                + evalthreads);
        output.systemMessage(seedMessage);

        return state;
    }

    /**
     * Begins a fresh evolutionary run with a given state. The state should have
     * been provided by initialize(...). The jobPrefix is added to the front of
     * output and checkpoint filenames. If it's null, nothing is added to the
     * front.
     */

    public static void cleanup(EvolutionState state) {
        // flush the output
        state.output.flush();

        // Possibly print out the run parameters
        PrintWriter pw = new PrintWriter(System.err);

        // before we print out access information, we need to still "get" these
        // parameters, so that they show up as accessed and gotten.
        state.parameters.getBoolean(new Parameter(P_PRINTUSEDPARAMETERS), null,
                false);
        state.parameters.getBoolean(new Parameter(P_PRINTACCESSEDPARAMETERS),
                null, false);
        state.parameters.getBoolean(new Parameter(P_PRINTUNUSEDPARAMETERS),
                null, false);
        state.parameters.getBoolean(new Parameter(P_PRINTUNACCESSEDPARAMETERS),
                null, false);
        state.parameters.getBoolean(new Parameter(P_PRINTALLPARAMETERS), null,
                false);

        // ...okay, here we go...

        if (state.parameters.getBoolean(new Parameter(P_PRINTUSEDPARAMETERS),
                null, false)) {
            pw.println("\n\nUsed Parameters\n===============\n");
            state.parameters.listGotten(pw);
        }

        if (state.parameters.getBoolean(
                new Parameter(P_PRINTACCESSEDPARAMETERS), null, false)) {
            pw.println("\n\nAccessed Parameters\n===================\n");
            state.parameters.listAccessed(pw);
        }

        if (state.parameters.getBoolean(new Parameter(P_PRINTUNUSEDPARAMETERS),
                null, false)) {
            pw.println("\n\nUnused Parameters\n"
                    + "================= (Ignore parent.x references) \n");
            state.parameters.listNotGotten(pw);
        }

        if (state.parameters.getBoolean(new Parameter(
                P_PRINTUNACCESSEDPARAMETERS), null, false)) {
            pw.println("\n\nUnaccessed Parameters\n"
                    + "===================== (Ignore parent.x references) \n");
            state.parameters.listNotAccessed(pw);
        }

        if (state.parameters.getBoolean(new Parameter(P_PRINTALLPARAMETERS),
                null, false)) {
            pw.println("\n\nAll Parameters\n==============\n");
            // list only the parameters visible. Shadowed parameters not shown
            state.parameters.list(pw, false);
        }

        pw.flush();

        System.err.flush();
        System.out.flush();

        // finish by closing down Output. This is because gzipped and other
        // buffered
        // streams just don't shut write themselves out, and finalize isn't
        // called
        // on them because Java's being obnoxious. Pretty stupid.
        state.output.close();
    }


    /** Top-level evolutionary loop. */

    public KevoreeIndividual start() {
        KevoreeIndividual ki = null;
        EvolutionState state;
        ParameterDatabase parameters;
        String[] args = new String[] { "-file",this.getClass().getClassLoader().getResource("kevoreeTest.params").getFile() };
        int currentJob = 0; // the next job number (0 by default)

        parameters = loadParameterDatabase(args);
        if (currentJob == 0) // no current job number yet
            currentJob = parameters.getIntWithDefault(new Parameter(
                    "current-job"), null, 0);
        if (currentJob < 0)
            Output.initialError("The 'current-job' parameter must be >= 0 (or not exist, which defaults to 0)");

        int numJobs = parameters.getIntWithDefault(new Parameter("jobs"), null,
                1);
        if (numJobs < 1)
            Output.initialError("The 'jobs' parameter must be >= 1 (or not exist, which defaults to 1)");

        for (int job = currentJob; job < numJobs; job++) {
            {

                // Initialize the EvolutionState, then set its job variables
                state = initialize(parameters, job); // pass in job# as the seed
                                                     // increment
                state.output.systemMessage("Job: " + job);
                state.job = new Object[1]; // make the job argument storage
                state.job[0] = new Integer(job); // stick the current job in our
                                                 // job storage
                state.runtimeArguments = args; // stick the runtime arguments in
                                               // our storage
                if (numJobs > 1) // only if iterating (so we can be
                                 // backwards-compatible),
                {
                    String jobFilePrefix = "job." + job + ".";
                    state.output.setFilePrefix(jobFilePrefix); // add a prefix
                                                               // for
                                                               // checkpoint/output
                                                               // files
                    state.checkpointPrefix = jobFilePrefix
                            + state.checkpointPrefix; // also set up checkpoint
                                                      // prefix
                }

                // Here you can set up the EvolutionState's parameters further
                // before it's setup(...).
                // This includes replacing the random number generators,
                // changing values in state.parameters,
                // changing instance variables (except for job and
                // runtimeArguments, please), etc.

                // now we let it go
                state.run(EvolutionState.C_STARTED_FRESH);
                ki = (KevoreeIndividual) ((SimpleStatistics)state.statistics).best_of_run[0];
                
//                printBestIndividuals(state);
                cleanup(state); // flush and close various streams, print out
                                // parameters if necessary
                parameters = null; // so we load a fresh database next time
                                   // around
//                bestIndividual.printIndividualForHumans(state, 0);
                
            }
            /*
             * catch (Throwable e) // such as an out of memory error caused by
             * this job { e.printStackTrace(); state = null; System.gc(); //
             * take a shot! }
             */
        }
        return ki;
    }

    
    private void printBestIndividuals(EvolutionState state){
        System.out.println("Best of run lenght : " + ((MultiObjectiveStatistics)state.statistics).best_of_run.length);
        
        for (Individual individual : ((MultiObjectiveStatistics)state.statistics).best_of_run) {
            ParserUtil.save("target/classes/modified.kev", ((KevoreeIndividual)individual).myModel);
//            App.startEditor("target/classes/modified.kev");
        }
    }
}
