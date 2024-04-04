/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.functionaltest.cucumber.report;

import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Node;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceParsed;
import io.cucumber.plugin.event.WriteEvent;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class VitamReporter implements ConcurrentEventListener {
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(VitamReporter.class);
    private static final String RESET_COLOR = "\033[0m";
    private static final String RED_COLOR = "\033[0;31m";
    private static final String GREEN_COLOR = "\033[0;32m";

    private final PrintStream out;
    private Reports reports = null;
    private Report report = null;
    private final Map<URI, String> featureNames = new HashMap<>();

    public VitamReporter(OutputStream out) {
        this.out = new PrintStream(out, false, StandardCharsets.UTF_8);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceParsed.class, this::sourceParsed);
        publisher.registerHandlerFor(TestRunStarted.class, this::runStarted);
        publisher.registerHandlerFor(TestRunFinished.class, this::runFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::caseFinished);
        publisher.registerHandlerFor(TestCaseStarted.class, this::caseStarted);
        publisher.registerHandlerFor(WriteEvent.class, this::writeEvent);
    }

    private void sourceParsed(TestSourceParsed event) {
        if (event.getNodes().size() != 1) {
            throw new CucumberException("There should be exactly one root node");
        }
        Node rootNode = event.getNodes().iterator().next();
        if (!(rootNode instanceof Feature)) {
            throw new CucumberException("Root node should be a feature");
        }
        Feature feature = (Feature) rootNode;
        featureNames.put(feature.getUri(), feature.getName().orElse(""));
    }

    private void runStarted(TestRunStarted event) {
        reports = new Reports();
    }

    private void runFinished(TestRunFinished event) {
        reports.setEnd(LocalDateTime.ofInstant(event.getInstant(), ZoneOffset.UTC));
        String finalReport = JsonHandler.prettyPrint(reports);
        out.print(finalReport);

        boolean success = reports.getReports().stream().allMatch(Report::isOK);
        logEvents("\n\n\n#######################\nFULL REPORT:\n#######################\n\n" + finalReport, success);
    }

    private void caseStarted(TestCaseStarted event) {
        report = new Report();
        report.setStart(LocalDateTime.ofInstant(event.getInstant(), ZoneOffset.UTC));
        report.setFeature(featureNames.get(event.getTestCase().getUri()));
    }

    private void caseFinished(TestCaseFinished event) {
        TestCase testCase = event.getTestCase();
        report.setDescription(testCase.getName());
        report.setTags(testCase.getTags().stream().map(tag -> tag.substring(1)).collect(Collectors.toList()));
        report.setEnd(LocalDateTime.ofInstant(event.getInstant(), ZoneOffset.UTC));
        Result result = event.getResult();
        Throwable error = result.getError();
        if (error != null) {
            report.addError(error.toString());
        }
        reports.add(report);

        String msg = "\n" + report.getFeature() + " - " + report.getDescription() +
            "\n" + JsonHandler.prettyPrint(report);
        logEvents(msg, result.getStatus().isOk());
    }

    private void writeEvent(WriteEvent writeEvent) {
        report.setOperationId(writeEvent.getText());
    }

    private static void logEvents(String msg, boolean success) {
        // LOG to file + print to CONSOLE for ansible
        if (success) {
            LOGGER.info(msg);
            System.out.println(GREEN_COLOR + msg + RESET_COLOR);
        } else {
            LOGGER.warn(msg);
            System.out.println(RED_COLOR + msg + RESET_COLOR);
        }
    }
}