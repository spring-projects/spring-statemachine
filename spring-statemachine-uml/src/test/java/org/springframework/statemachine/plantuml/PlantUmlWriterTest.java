package org.springframework.statemachine.plantuml;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;
import org.springframework.util.ObjectUtils;

import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.plantuml.PlantUmlWriterParameters.Direction.RIGHT;
import static org.springframework.statemachine.plantuml.PlantUmlWriterParameters.Direction.UP;

class PlantUmlWriterTest {

    private static final Log log = LogFactory.getLog(PlantUmlWriterTest.class);

    private static AnnotationConfigApplicationContext context;

    @BeforeAll
    public static void setup() {
        context = new AnnotationConfigApplicationContext();
        // adding ALL beans needed by ALL .uml files
        context.register(BeansForUmlFiles.class);
        // refreshing context
        context.refresh();
    }

    /**
     * Commented UML files correspond to test cases for which PlantUmlWriter is not yet 'ready'
     *
     * @return stream of "uml resource path"
     */
    public static Stream<Arguments> plantUmlTestMethodSource() {
        return Stream.of(
                Arguments.of("org/springframework/statemachine/uml/action-with-transition-choice", null),
                Arguments.of("org/springframework/statemachine/uml/action-with-transition-junction", null),
                Arguments.of("org/springframework/statemachine/uml/broken-model-shadowentries", null),
                Arguments.of("org/springframework/statemachine/uml/initial-actions", null),
                Arguments.of("org/springframework/statemachine/uml/missingname-choice", null),
                Arguments.of("org/springframework/statemachine/uml/multijoin-forkjoin", null),
                Arguments.of("org/springframework/statemachine/uml/pseudostate-in-submachine", null),
                Arguments.of("org/springframework/statemachine/uml/pseudostate-in-submachineref", null),
                Arguments.of("org/springframework/statemachine/uml/simple-actions", null),
                Arguments.of("org/springframework/statemachine/uml/simple-choice", null),
                // It seems Spring statemachine does not support org.eclipse.uml2.uml.ConnectionPointReference !
                // Arguments.of("org/springframework/statemachine/uml/simple-connectionpointref", null),
                Arguments.of("org/springframework/statemachine/uml/simple-entryexit", null),
                Arguments.of("org/springframework/statemachine/uml/simple-eventdefer", null),
                Arguments.of("org/springframework/statemachine/uml/simple-flat-end", null),
                Arguments.of("org/springframework/statemachine/uml/simple-flat-multiple-to-end-viachoices", null),
                Arguments.of("org/springframework/statemachine/uml/simple-flat-multiple-to-end", null),
                Arguments.of("org/springframework/statemachine/uml/simple-flat", null),
                Arguments.of("org/springframework/statemachine/uml/simple-forkjoin",
                        // add hidden transition to make diagram more readable
                        new PlantUmlWriterParameters<String>()
                                .hiddenTransition("S1", RIGHT, "S2")
                ),
                Arguments.of("org/springframework/statemachine/uml/simple-guards", null),
                Arguments.of("org/springframework/statemachine/uml/simple-history-deep", null),
                Arguments.of("org/springframework/statemachine/uml/simple-history-default", null),
                Arguments.of("org/springframework/statemachine/uml/simple-history-shallow", null),
                Arguments.of("org/springframework/statemachine/uml/simple-junction", null),
                Arguments.of("org/springframework/statemachine/uml/simple-localtransition",
                        // add some parameters to make diagram more readable
                        new PlantUmlWriterParameters<String>()
                                .arrowDirection("S22", UP, "S2")
                                .arrowDirection("S21", UP, "S2")
                                .arrowDirection("S22", UP, "S2")
                                .arrowDirection("S21", UP, "S2")
                ),
                // It seems Spring statemachine UML parser creates duplicated transitions!
                // Arguments.of("org/springframework/statemachine/uml/simple-root-regions", null),
                Arguments.of("org/springframework/statemachine/uml/simple-spels", null),
                Arguments.of("org/springframework/statemachine/uml/simple-state-actions", null),
                Arguments.of("org/springframework/statemachine/uml/simple-submachine", null),
                Arguments.of("org/springframework/statemachine/uml/simple-submachineref", null),
                Arguments.of("org/springframework/statemachine/uml/simple-timers", null),
                Arguments.of("org/springframework/statemachine/uml/simple-transitiontypes", null),
                Arguments.of("org/springframework/statemachine/uml/transition-effect-spel", null)
        );
    }

    @ParameterizedTest
    @MethodSource("plantUmlTestMethodSource")
    void plantUmlTest(String resourcePath, PlantUmlWriterParameters<String> plantUmlWriterParameters) throws Exception {


        // Loading statemachine from uml
        Resource umlResource = new ClassPathResource(resourcePath + ".uml");
        assertThat(umlResource.exists()).isTrue();
        UmlStateMachineModelFactory umlStateMachineModelFactory = new UmlStateMachineModelFactory(umlResource);
        // make umlStateMachineModelFactory aware of beans available in BeansForUmlFiles.class
        umlStateMachineModelFactory.setBeanFactory(context);

        // Dumping statemachine to PlantUml diagram
        String stateMachineAsPlantUML = new PlantUmlWriter<String, String>()
                .toPlantUml(
                        StateMachineHelper.buildStateMachine(umlStateMachineModelFactory),
                        null,
                        plantUmlWriterParameters
                );
        log.info("\n" + stateMachineAsPlantUML);

        // comparing with expected .puml diagram

        Resource pumlResource = new ClassPathResource(resourcePath + ".puml");
        assertThat(pumlResource.exists()).isTrue();

        String expectedPlantUmlDiagram = new String(Files.readAllBytes(pumlResource.getFile().toPath()));
        assertThat(normalizeNewLines(stateMachineAsPlantUML))
                .isEqualTo(normalizeNewLines(expectedPlantUmlDiagram));
    }

    /**
     * Normalizing 'new line characters'<BR/>
     * Whatever might be the 'new line character' in the input string (CR/LF/CRLF)<BR/>
     * it will be replaced by LF.
     *
     * @param string String to normalize
     * @return input string with line endings being '\n' ( 'LF' )
     */
    private static String normalizeNewLines(String string) {
        return string.replace("\r\n", "\n").replace('\r', '\n');
    }

    @Configuration
    public static class BeansForUmlFiles {

        @Bean
        public ChoiceGuard s2Guard() {
            return new ChoiceGuard("s2");
        }

        @Bean
        public ChoiceGuard s3Guard() {
            return new ChoiceGuard("s3");
        }

        @Bean
        public ChoiceGuard s5Guard() {
            return new ChoiceGuard("s5");
        }

        @Bean
        public ChoiceGuard choice2Guard() {
            return new ChoiceGuard("choice2");
        }

        @Bean
        public SimpleGuard denyGuard() {
            return new SimpleGuard(false);
        }

        @Bean
        public LatchAction s1ToChoice() {
            return new LatchAction();
        }

        @Bean
        public LatchAction choiceToS2() {
            return new LatchAction();
        }

        @Bean
        public LatchAction choiceToS4() {
            return new LatchAction();
        }

        @Bean
        public LatchAction choice1ToChoice2() {
            return new LatchAction();
        }

        @Bean
        public LatchAction choiceToS5() {
            return new LatchAction();
        }

        @Bean
        public LatchAction choiceToS6() {
            return new LatchAction();
        }

        @Bean
        public LatchAction action1() {
            return new LatchAction();
        }

        @Bean
        public JunctionGuard s6Guard() {
            return new JunctionGuard("s6");
        }

        @Bean
        public LatchAction initialAction() {
            return new LatchAction();
        }

        @Bean
        public LatchAction e1Action() {
            return new LatchAction();
        }

        @Bean
        public LatchAction e2Action() {
            return new LatchAction();
        }

        @Bean
        public LatchAction s1Exit() {
            return new LatchAction();
        }

        @Bean
        public LatchAction s2Entry() {
            return new LatchAction();
        }

        @Bean
        public LatchAction s3Entry() {
            return new LatchAction();
        }

        @Bean
        public LatchAction s5Entry() {
            return new LatchAction();
        }
    }

    // --------------------------------------------------------------------------------
    // Actions
    // --------------------------------------------------------------------------------

    @Setter
    @Getter
    public static class LatchAction implements Action<String, String>, BeanNameAware {

        private String beanName;

        CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void execute(StateContext<String, String> context) {
            latch.countDown();
        }

    }

    // --------------------------------------------------------------------------------
    // Guards
    // --------------------------------------------------------------------------------

    @Setter
    @Getter
    public static class ChoiceGuard implements Guard<String, String>, BeanNameAware {

        private String beanName;

        private final String match;

        public ChoiceGuard(String match) {
            this.match = match;
        }

        @Override
        public boolean evaluate(StateContext<String, String> context) {
            return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("choice", String.class));
        }
    }

    @Setter
    @Getter
    public static class SimpleGuard implements Guard<String, String>, BeanNameAware {

        private String beanName;

        private final boolean deny;

        public SimpleGuard(boolean deny) {
            this.deny = deny;
        }

        @Override
        public boolean evaluate(StateContext<String, String> context) {
            return deny;
        }
    }

    @Setter
    @Getter
    public static class JunctionGuard implements Guard<String, String>, BeanNameAware {

        private String beanName;

        private final String match;

        public JunctionGuard(String match) {
            this.match = match;
        }

        @Override
        public boolean evaluate(StateContext<String, String> context) {
            return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("junction", String.class));
        }
    }

}