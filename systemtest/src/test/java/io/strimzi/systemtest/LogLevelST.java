package io.strimzi.systemtest;


import io.strimzi.api.kafka.model.Kafka;
import io.strimzi.test.ClusterOperator;
import io.strimzi.test.Namespace;
import io.strimzi.test.StrimziRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;


import static junit.framework.TestCase.assertTrue;

@RunWith(StrimziRunner.class)
@Namespace(LogLevelST.NAMESPACE)
@ClusterOperator
public class LogLevelST extends AbstractST {
    static final String NAMESPACE = "log-level-cluster-test";
    private static final Logger LOGGER = LogManager.getLogger(LogLevelST.class);
    private static final String TESTED_LOGGER = "kafka.root.logger.level";
    private static final String LOG_LEVEL = "LOG_LEVEL";
    private static final String RESOURCE_TYPE = "statefulset";
    private static final String POD_NAME = kafkaClusterName(CLUSTER_NAME) + "-0";

    @Test
    void testLogLevel(){
        LOGGER.info("Running testLogLevel in namespace {}", NAMESPACE);

        resources().kafka(resources().defaultKafka(CLUSTER_NAME, 1)
            .editSpec()
                .editKafka().
                    addToConfig(TESTED_LOGGER, LOG_LEVEL)
                .endKafka()
            .endSpec()
        .build()).done();

//        String logLevel = (String)kafka.getSpec().getKafka().getConfig().get(TESTED_LOGGER);
//        boolean result = logLevel.equals(LOG_LEVEL);

        String kafkaPodLog = kubeClient.logs(POD_NAME, "kafka");

        boolean result = kafkaPodLog.contains("level="+LOG_LEVEL);


        if(!result) {
            kafkaPodLog = kubeClient.searchInLog(RESOURCE_TYPE, kafkaClusterName(CLUSTER_NAME), 600, LOG_LEVEL);
            result = kafkaPodLog.isEmpty();
        }

        assertTrue("Kafka's log level is set properly", result);
    }
}
