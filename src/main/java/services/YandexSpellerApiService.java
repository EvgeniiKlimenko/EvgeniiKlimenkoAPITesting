package services;

import model.YandexSpellerAnswer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import constants.Format;
import constants.Language;
import constants.Option;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import utils.PropertiesHolder;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static constants.ParameterName.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class YandexSpellerApiService {
    public static final URI SPELLER_CHECK_TEXT_URI = URI
            .create(PropertiesHolder.PROPS.getProperty("speller.checkText.url"));
    public static final URI SPELLER_CHECK_TEXTS_URI = URI
            .create(PropertiesHolder.PROPS.getProperty("speller.checkTexts.url"));
    private static long requestNumber = 0L;
    private Method requestMethod;
    private Map<String, String> parameters;

    private YandexSpellerApiService(Map<String, String> parameters, Method method) {
        this.parameters = parameters;
        this.requestMethod = method;
    }

    public static ApiRequestBuilder requestBuilder() {
        return new ApiRequestBuilder();
    }


    public static class ApiRequestBuilder {
        private Map<String, String> parameters = new HashMap<>();
        private Method requestMethod = Method.GET;  // by default I send GET

        public ApiRequestBuilder setMethod(Method method) {
            requestMethod = method;
            return this;
        }

        public ApiRequestBuilder setLanguage(Language... lang) {
            parameters.put(LANGUAGE, Arrays.stream(lang).map(l -> l.value).collect(Collectors.joining(", ")));
            return this;
        }

        public ApiRequestBuilder setFormat(Format format) {
            parameters.put(FORMAT, format.format);
            return this;
        }

        public ApiRequestBuilder setOptions(Option... options) {
            int resultParameter = 0;
            for (Option o : options) resultParameter += o.value;
            parameters.put(OPTIONS, String.valueOf(resultParameter));
            return this;
        }

        public ApiRequestBuilder setText(String... text) {
            parameters.put(TEXT, Arrays.stream(text).collect(Collectors.joining(", ")));
            return this;
        }

        public YandexSpellerApiService buildRequest() {
            return new YandexSpellerApiService(parameters, requestMethod);
        }

    }


    public static Response setUpAndSendRequest(Method method, Language lang, String... text) {
        return requestBuilder()
                .setLanguage(lang)
                .setText(text)
                .setMethod(method)
                .buildRequest()
                .sendRequest();
    }

    public static Response setUpAndSendRequest(Language lang, String... text) {
        return requestBuilder()
                .setLanguage(lang)
                .setText(text)
                .buildRequest()
                .sendRequest();
    }

    public static Response setUpAndSendRequest(Language lang, Option opt, String... text) {
        return requestBuilder()
                .setLanguage(lang)
                .setText(text)
                .setOptions(opt)
                .buildRequest()
                .sendRequest();
    }

    public static Response setUpAndSendRequest(Language lang, Format format, String... text) {
        return requestBuilder()
                .setLanguage(lang)
                .setText(text)
                .setFormat(format)
                .buildRequest()
                .sendRequest();
    }

    public static Response setUpAndSendTextsRequest(Language lang, String... text) {
        return requestBuilder()
                .setLanguage(lang)
                .setText(text)
                .buildRequest()
                .sendCheckTextsRequest();
    }

    public Response sendRequest() {
        return RestAssured
                .given(requestSpecification()).log().all()
                .queryParams(parameters)
                .request(requestMethod, SPELLER_CHECK_TEXT_URI)
                .prettyPeek();
    }

    public Response sendCheckTextsRequest() {
        return RestAssured
                .given(requestSpecification()).log().all()
                .queryParams(parameters)
                .request(requestMethod, SPELLER_CHECK_TEXTS_URI)
                .prettyPeek();
    }

    public static YandexSpellerAnswer getTheOnlyAnswer(Response response) {
        List<YandexSpellerAnswer> answers = new Gson()
                .fromJson(response.asString().trim(), new TypeToken<List<YandexSpellerAnswer>>() {
                }.getType());
        assertThat("We expect to get one answer, but got " + answers.size(), answers, hasSize(1));
        return answers.get(0);
    }

    public static List<YandexSpellerAnswer> getAnswers(Response response) {
        return new Gson()
                .fromJson(response.asString().trim(), new TypeToken<List<YandexSpellerAnswer>>() {
                }.getType());
    }

    public static List<String> getStringResult(Response response) {
        return getAnswers(response).stream()
                .map(yandexSpellerAnswer -> yandexSpellerAnswer.getWord())
                .collect(Collectors.toList());
    }

    public static RequestSpecification requestSpecification() {
        return new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .addQueryParam("requestNumber", ++requestNumber)
                .setBaseUri(SPELLER_CHECK_TEXT_URI)
                .build();
    }

    public static ResponseSpecification goodResponseSpecification() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(10000L))
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification badResponseSpecification() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.TEXT)
                .expectResponseTime(lessThan(10000L))
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }

    public static List<List<YandexSpellerAnswer>> getAnswersArray(Response response) {
        return new Gson()
                .fromJson(response.asString().trim(), new TypeToken<List<List<YandexSpellerAnswer>>>() {
                }.getType());
    }

    public static List<String> getStringResultArray(Response response) {
        List<List<YandexSpellerAnswer>> fromJson = getAnswersArray(response);
        List<String> wordList = new ArrayList<String>();
        fromJson.stream()
                .forEach(yandexSpellerAnswerArray -> yandexSpellerAnswerArray.stream()
                        .forEach(yandexSpeller -> wordList.add(yandexSpeller.getWord())));
        return wordList;
    }

    public static List<String> getStringSuggestionsArray(Response response) {
        List<List<YandexSpellerAnswer>> fromJson = getAnswersArray(response);
        List<String> suggestionsList = new ArrayList<String>();
        fromJson.stream()
                .forEach(yandexSpellerAnswer -> yandexSpellerAnswer.stream()
                        .forEach(yandexAnswer -> yandexAnswer.getS()
                                .forEach(suggestion -> suggestionsList.add(suggestion))));
        return suggestionsList;
    }

}
