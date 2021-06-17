package com.innometrics.gqm_api.metrics_generation;

import com.google.common.annotations.VisibleForTesting;
import com.innometrics.gqm_api.dto.MetricResponse;
import com.innometrics.gqm_api.dto.QuestionBaseDto;
import com.innometrics.gqm_api.dto.QuestionGenerateMetricsRequest;
import com.innometrics.gqm_api.model.Metric;
import com.innometrics.gqm_api.repositories.MetricRepository;
import com.innometrics.gqm_api.settings.FileGenerationConfiguration;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.*;
import mulan.classifier.MultiLabelOutput;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.MultiLabelInstances;
import org.springframework.stereotype.Component;
import weka.classifiers.trees.J48;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.innometrics.gqm_api.metrics_generation.MetricsGenerator.FileType.DATA;
import static com.innometrics.gqm_api.metrics_generation.MetricsGenerator.FileType.INPUT;
import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.concat;

@Component
@RequiredArgsConstructor
public class MetricsGenerator {

    private static final Integer MIN_METRICS_FOR_MULAN_LIBRARY_TO_WORK = 3;

    private final MetricRepository metricRepository;
    private final FileGenerationConfiguration fileGenerationConfiguration;

    private StanfordCoreNLP pipeline;
    private Set<String> stopWords;
    private boolean StanfordCoreNlpObjectCreated = false;

    /**
     * @param inputQuestionContent          content of the question for which metrics should be generated
     * @param questionsWithoutInputQuestion list of question objects with content and set of metrics without input one
     * @return list of generated metrics ids
     */
    public List<Long> generateMetrics(
            String inputQuestionContent,
            List<QuestionGenerateMetricsRequest> questionsWithoutInputQuestion
    ) {
        val metricsNames = getMetricsNamesForQuestions(questionsWithoutInputQuestion);
        if (metricsNames.size() < MIN_METRICS_FOR_MULAN_LIBRARY_TO_WORK) {
            return emptyList();
        }

        val questionContentToSetOfMetricsMap = createQuestionToMetricsMapping(questionsWithoutInputQuestion);
        generateFiles(
                getPreprocessed(inputQuestionContent),
                getPreprocessedQuestionsContents(questionContentToSetOfMetricsMap),
                metricsNames
        );
        return getMetricsIds(makePredictions(trainClassifier()), metricsNames);
    }

    /**
     * @param questions list of question objects with content and set of metrics
     * @return list of distinct metrics from all question objects
     */
    private List<String> getMetricsNamesForQuestions(List<QuestionGenerateMetricsRequest> questions) {
        return questions.stream()
                        .flatMap(q -> q.getMetrics().stream())
                        .map(MetricResponse::getName)
                        .distinct()
                        .collect(Collectors.toList());
    }

    /**
     * @param questions list of question objects with content and set of metrics
     * @return dictionary with questions contents as keys and set of metrics as values
     */
    private Map<String, Set<String>> createQuestionToMetricsMapping(
            List<QuestionGenerateMetricsRequest> questions
    ) {
        return questions
                .stream()
                .collect(toMap(
                        QuestionBaseDto::getContent,
                        question -> question.getMetrics().stream().map(MetricResponse::getName).collect(toSet()),
                        (oldMetrics, newMetrics) -> concat(oldMetrics.stream(), newMetrics.stream()).collect(toSet())
                ));
    }

    /**
     * @param questionToMetricsMap dictionary with questions contents as keys and set of metrics as values
     * @return dictionary with preprocessed questions contents as keys and set of metrics as values
     */
    private Map<String, Set<String>> getPreprocessedQuestionsContents(Map<String, Set<String>> questionToMetricsMap) {
        return questionToMetricsMap
                .keySet().stream()
                .collect(toMap(
                        this::getPreprocessed,
                        questionToMetricsMap::get
                ));
    }

    /**
     * @param content the value of the question content
     * @return preprocessed value of the question content
     */
    private String getPreprocessed(String content) {
        return reduceToStandardForm(getCleanedText(content));
    }

    /**
     * @param content the value of the question content
     * @return string with meaningful words only which consist only of small letters
     */
    @VisibleForTesting
    private String getCleanedText(String content) {
        val stopWords = getStopWords();
        return Arrays.stream(content.toLowerCase().split(" "))
                     .map(el -> el.replaceAll("[^a-z]", ""))
                     .filter(el -> !stopWords.contains(el))
                     .collect(joining(" "));
    }

    /**
     * @return set of no meaningful words which can be deleted without the loss of meaning
     */
    @SneakyThrows
    private Set<String> getStopWords() {
        if (stopWords == null) {
            stopWords = new HashSet<>(Files.readAllLines(Paths.get(
                    "src/main/resources/static/english_stopwords.txt"
            )));
        }
        return stopWords;
    }

    /**
     * @param content the value of the question content
     * @return string with lemmatized words (reduced to standard form depending on the part of speech)
     */
    private String reduceToStandardForm(String content) {
        if (!StanfordCoreNlpObjectCreated) {
            createStanfordLemmatizer();
        }
        return lemmatize(content);
    }

    /**
     * create StanfordCoreNLP object properties, with POS tagging
     */
    private void createStanfordLemmatizer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(props);
        StanfordCoreNlpObjectCreated = true;
    }

    /**
     * @param content the value of the question content
     * @return string with lemmatized words (reduced to standard form depending on the part of speech)
     */
    private String lemmatize(String content) {
        val sentence = new Annotation(content);
        this.pipeline.annotate(sentence);

        val lemmasBuilder = new StringBuilder();
        for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
            lemmasBuilder.append(token.get(LemmaAnnotation.class))
                         .append(" ");
        }

        return lemmasBuilder.toString().trim();
    }

    /**
     * generates ARRF and HTML files to pass as an input in the Mulan library
     *
     * @param preprocessedInputQuestionContent content of the question for which metrics should be generated
     * @param preprocessedQuestionToMetricsMap keys - preprocessed questions contents, values - set of metrics
     * @param questionsMetricsNames            list of distinct metrics from all question objects
     */
    private void generateFiles(
            String preprocessedInputQuestionContent,
            Map<String, Set<String>> preprocessedQuestionToMetricsMap,
            List<String> questionsMetricsNames
    ) {
        createArrfFile(
                DATA,
                preprocessedInputQuestionContent,
                preprocessedQuestionToMetricsMap,
                questionsMetricsNames
        );

        createArrfFile(
                INPUT,
                preprocessedInputQuestionContent,
                preprocessedQuestionToMetricsMap,
                questionsMetricsNames
        );

        createXmlFile(questionsMetricsNames);
    }

    /**
     * create ARRF file from dataset or from input depending on the identifier
     *
     * @param identifier                    define the type of file (input file or data set file)
     * @param preprocessedInputQuestionContent preprocessed content of the input question
     * @param preprocessedQuestionToMetricsMap keys - preprocessed questions contents, values - set of metrics
     * @param questionMetricsNames             list of distinct metrics from all question objects
     */
    @SneakyThrows
    private void createArrfFile(
            FileType identifier,
            String preprocessedInputQuestionContent,
            Map<String, Set<String>> preprocessedQuestionToMetricsMap,
            List<String> questionMetricsNames
    ) {
        val fileContents = createArrfFileContents(
                identifier,
                preprocessedInputQuestionContent,
                preprocessedQuestionToMetricsMap,
                questionMetricsNames
        );

        BufferedWriter writer = new BufferedWriter(
                new FileWriter(
                        fileGenerationConfiguration
                                .getMulanInputFilesDirectory() + "/arffFile" + identifier.getFileName() + ".arrf"
                )
        );
        writer.write(fileContents);
        writer.close();
    }

    /**
     * @param identifier                       define the type of file (input file or data set file)
     * @param preprocessedInputQuestionContent preprocessed content of the input question
     * @param preprocessedQuestionToMetricsMap keys - preprocessed questions contents, values - set of metrics
     * @param questionMetricsNames             list of distinct metrics from all question objects
     * @return content of the file
     */
    private String createArrfFileContents(
            FileType identifier,
            String preprocessedInputQuestionContent,
            Map<String, Set<String>> preprocessedQuestionToMetricsMap,
            List<String> questionMetricsNames
    ) {
        val fileContentsBuilder = new StringBuilder();

        fileContentsBuilder.append(format(
                "@relation %s\n\n", identifier.getRelationTitle()
        ));

        val listOfAttributes = getListOfAttributesFrom(
                preprocessedInputQuestionContent,
                preprocessedQuestionToMetricsMap,
                questionMetricsNames
        );
        val attributeToColumnIdMap = getAttributeToColumnIdMap(listOfAttributes);
        addAttributesPartOfFile(fileContentsBuilder, listOfAttributes);

        addDataPartOfFile(
                fileContentsBuilder,
                identifier.getQuestionToMetricsMapRetriever().apply(
                        preprocessedQuestionToMetricsMap,
                        preprocessedInputQuestionContent
                ),
                listOfAttributes,
                attributeToColumnIdMap
        );

        return fileContentsBuilder.toString();
    }

    /**
     * @param preprocessedInputQuestionContent preprocessed content of the input question
     * @param preprocessedQuestionToMetricsMap keys - preprocessed questions contents, values - set of metrics
     * @param questionMetricsNames             list of distinct metrics from all question objects
     * @return attributes for file = concatenated list of distinct contents tokens and metrics
     */
    private List<String> getListOfAttributesFrom(
            String preprocessedInputQuestionContent,
            Map<String, Set<String>> preprocessedQuestionToMetricsMap,
            List<String> questionMetricsNames
    ) {
        val listOfDistinctQuestionsContentsTokens = createListOfDistinctTokens(
                preprocessedInputQuestionContent,
                preprocessedQuestionToMetricsMap
        );
        return Stream
                .of(listOfDistinctQuestionsContentsTokens, questionMetricsNames)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * @param preprocessedInputQuestionContent preprocessed content of the input question
     * @param preprocessedQuestionToMetricsMap keys - preprocessed questions contents, values - set of metrics
     * @return list of distinct words, collected from the questions contents
     */
    private List<String> createListOfDistinctTokens(
            String preprocessedInputQuestionContent,
            Map<String, Set<String>> preprocessedQuestionToMetricsMap
    ) {
        return concat(
                Stream.of(preprocessedInputQuestionContent),
                preprocessedQuestionToMetricsMap.keySet().stream()
        ).flatMap(str -> Arrays.stream(str.split(" ")))
         .distinct()
         .collect(Collectors.toList());
    }

    private Map<String, Integer> getAttributeToColumnIdMap(
            List<String> listOfAttributes
    ) {
        Map<String, Integer> attributeToColumnIdMap = new HashMap<>();
        for (val attribute : listOfAttributes) {
            attributeToColumnIdMap.put(attribute, listOfAttributes.indexOf(attribute));
        }
        return attributeToColumnIdMap;
    }

    /**
     * add attributes part to the file
     *
     * @param fileContentsBuilder    content of the file
     * @param listOfAttributes       attributes for file = concatenated list of distinct contents tokens and metrics
     */
    private void addAttributesPartOfFile(
            StringBuilder fileContentsBuilder,
            List<String> listOfAttributes
    ) {
        for (val attribute : listOfAttributes) {
            fileContentsBuilder
                    .append("@attribute ")
                    .append(attribute.replaceAll("\\s+", ""))
                    .append(" {0, 1}\n");
        }
    }


    /**
     * @param fileContentsBuilder              content of the file
     * @param preprocessedQuestionToMetricsMap keys - preprocessed questions contents, values - set of metrics
     * @param listOfAttributes                 attributes for file = concatenated list of distinct contents tokens and metrics
     * @param attributeToColumnIdMap           key - attribute, value - it's index in the listOfAttributes
     */
    private void addDataPartOfFile(
            StringBuilder fileContentsBuilder,
            Map<String, Set<String>> preprocessedQuestionToMetricsMap,
            List<String> listOfAttributes,
            Map<String, Integer> attributeToColumnIdMap
    ) {
        fileContentsBuilder.append("\n@data\n");

        for (val entry : preprocessedQuestionToMetricsMap.entrySet()) {
            val metricsRow = Arrays.stream(new int[listOfAttributes.size()]).boxed().collect(Collectors.toList());

            val keyWords = Arrays.stream(entry.getKey().split(" ")).collect(Collectors.toList());
            keyWords.forEach(w -> metricsRow.set(attributeToColumnIdMap.get(w), 1));

            val metricsForQ = entry.getValue();
            metricsForQ.forEach(m -> metricsRow.set(attributeToColumnIdMap.get(m), 1));

            fileContentsBuilder.append(
                    metricsRow.stream().map(Object::toString).collect(joining(","))
            ).append("\n");
        }
    }

    /**
     * @param questionMetricsNames list of distinct metrics from all question objects
     */
    @SneakyThrows
    private void createXmlFile(List<String> questionMetricsNames) {
        val fileContents = createXmlFileContent(questionMetricsNames);
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(
                        fileGenerationConfiguration.getMulanInputFilesDirectory() + "/xmlFile.xml"
                )
        );
        writer.write(fileContents);
        writer.close();
    }

    /**
     *
     * @param questionMetricsNames list of distinct metrics from all question objects
     * @return content to write to the file
     */
    private String createXmlFileContent(List<String> questionMetricsNames) {
        val fileContentsBuilder = new StringBuilder();
        fileContentsBuilder.append(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<labels xmlns=\"http://mulan.sourceforge.net/labels\">\n"
        );

        for (val m : questionMetricsNames) {
            fileContentsBuilder
                    .append("<label name=\"")
                    .append(m.replaceAll("\\s+", ""))
                    .append("\"></label>\n");
        }

        fileContentsBuilder.append("</labels>");

        return fileContentsBuilder.toString();
    }

    /**
     *
     * @return RAkEl model
     */
    @SneakyThrows
    private RAkEL trainClassifier() {
        //Load data
        val arrfDataFilePath = fileGenerationConfiguration.getMulanInputFilesDirectory() + "/arffFileData.arrf";
        val xmlFilePath = fileGenerationConfiguration.getMulanInputFilesDirectory() + "/xmlFile.xml";
        MultiLabelInstances dataset = new MultiLabelInstances(arrfDataFilePath, xmlFilePath);

        //create an instance of the learning algorithm
        RAkEL model = new RAkEL(new LabelPowerset(new J48()));

        //train the classifier
        model.setSizeOfSubset(2);
        model.build(dataset);

        return model;
    }

    /**
     *
     * @param model RAkEl model
     * @return list of boolean values which identify which metrics were generated to the question
     */
    @SneakyThrows
    private boolean[] makePredictions(RAkEL model) {
        //Load input
        val arrfInputFilePath = fileGenerationConfiguration.getMulanInputFilesDirectory() + "/arffFileInput.arrf";
        val xmlFilePath = fileGenerationConfiguration.getMulanInputFilesDirectory() + "/xmlFile.xml";
        MultiLabelInstances unlabeledData = new MultiLabelInstances(arrfInputFilePath, xmlFilePath);

        boolean[] bipartion = new boolean[0];
        int numInstances = unlabeledData.getNumInstances();
        for (int instanceIndex = 0; instanceIndex < numInstances; instanceIndex++) {
            val instance = unlabeledData.getDataSet().instance(instanceIndex);
            MultiLabelOutput output = model.makePrediction(instance);
            if (output.hasBipartition()) {
                bipartion = output.getBipartition();
            }
        }

        return bipartion;
    }

    /**
     *
     * @param bipartion list of boolean values which identify which metrics were generated to the question
     * @param metricsNames list of metrics names
     * @return list of ids of chosen metrics
     */
    private List<Long> getMetricsIds(boolean[] bipartion, List<String> metricsNames) {
        val metricsNamesToReturn = new ArrayList<String>();
        for (int i = 0; i < metricsNames.size(); ++i) {
            if (bipartion[i]) {
                metricsNamesToReturn.add(metricsNames.get(i));
            }
        }

        return metricRepository.findAllByNameIn(metricsNamesToReturn)
                               .stream()
                               .map(Metric::getId)
                               .collect(Collectors.toList());
    }

    @Getter
    @AllArgsConstructor
    public enum FileType{
        DATA(
                "Data",
                "InfoFromTheDataset",
                (map, str) -> map
        ),
        INPUT(
                "Input",
                "InfoFromTheInput",
                (map, str) -> singletonMap(str, emptySet())
        );

        private final String fileName;
        private final String relationTitle;
        private final BiFunction<Map<String, Set<String>>, String, Map<String, Set<String>>> questionToMetricsMapRetriever;
    }

}
