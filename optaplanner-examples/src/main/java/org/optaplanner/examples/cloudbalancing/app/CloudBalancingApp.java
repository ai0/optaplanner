/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.cloudbalancing.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.domain.CloudProcess;
import org.optaplanner.examples.cloudbalancing.persistence.CloudBalanceXmlSolutionFileIO;
import org.optaplanner.examples.cloudbalancing.swingui.CloudBalancingPanel;
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

/**
 * For an easy example, look at {@link CloudBalancingHelloWorld} instead.
 */
public class CloudBalancingApp extends CommonApp<CloudBalance> {

    public static final String SOLVER_CONFIG = "org/optaplanner/examples/cloudbalancing/solver/cloudBalancingSolverConfig.xml";

    public static final String DATA_DIR_NAME = "cloudbalancing";

    public static void main(String[] args) {
        prepareSwingEnvironment();
        CloudBalancingApp app = new CloudBalancingApp();
        app.init();
        app.solutionBusiness.setSolution(new CloudBalance());
        app.solutionBusiness.setSolutionFileName("Untitled");
        app.solverAndPersistenceFrame.setSolutionLoaded(null);
    }

    public CloudBalancingApp() {
        super("VM Placement",
                "Assign VMs to cluster.\n\n" +
                        "Each Compute Host must have enough hardware to run all of its VMs.\n" +
                        "Each used Compute Host inflicts a latency.",
                SOLVER_CONFIG, DATA_DIR_NAME,
                CloudBalancingPanel.LOGO_PATH);
    }

    @Override
    protected SolverFactory<CloudBalance> createSolverFactory() {
        return createSolverFactoryByApi();
    }

    protected SolverFactory<CloudBalance> createSolverFactoryByXml() {
        return SolverFactory.createFromXmlResource(SOLVER_CONFIG);
    }

    protected SolverFactory<CloudBalance> createSolverFactoryByApi() {
        if (this.optimizer != null) System.out.printf("Optimization Algorithm: %s\n", this.optimizer);

        SolverConfig solverConfig = new SolverConfig();

        solverConfig.setSolutionClass(CloudBalance.class);
        solverConfig.setEntityClassList(Collections.singletonList(CloudProcess.class));

        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setScoreDrlList(
                Arrays.asList("org/optaplanner/examples/cloudbalancing/solver/cloudBalancingConstraints.drl"));
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);

        solverConfig.setTerminationConfig(new TerminationConfig().withMinutesSpentLimit(2L));
        List<PhaseConfig> phaseConfigList = new ArrayList<>();

        ConstructionHeuristicPhaseConfig constructionHeuristicPhaseConfig = new ConstructionHeuristicPhaseConfig();
        constructionHeuristicPhaseConfig.setConstructionHeuristicType(this.optimizer);
        phaseConfigList.add(constructionHeuristicPhaseConfig);

        solverConfig.setPhaseConfigList(phaseConfigList);
        return SolverFactory.create(solverConfig);
    }

    @Override
    protected CloudBalancingPanel createSolutionPanel() {
        return new CloudBalancingPanel();
    }

    @Override
    public SolutionFileIO<CloudBalance> createSolutionFileIO() {
        return new CloudBalanceXmlSolutionFileIO();
    }

}
