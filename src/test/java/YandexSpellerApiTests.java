import io.restassured.response.Response;
import model.YandexSpellerAnswer;
import constants.AnswerField;
import constants.Language;
import core.DataProvidersForSpeller;
import io.restassured.http.Method;
import org.hamcrest.MatcherAssert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static constants.Option.*;
import static constants.SpellerErrorCode.ERROR_UNKNOWN_WORD;
import static constants.Format.HTML;
import static constants.Format.INCORRECT_FORMAT;
import static constants.Language.*;
import static constants.Texts.*;
import static core.YandexSpellerServiceObj.*;
import static org.hamcrest.Matchers.*;

public class YandexSpellerApiTests {
    /*
     * КТРЛ+АЛЬТ+Л (+)
     * Второй вариант ответа (+)
     * Структура проекта    (+)
     * SPELLER_URI в проперти и сделать класс контейнер (+)
     * Пару тестов для checkTexts(...)
     *
     * To generate pojo from JSON with terminal: mvn jsonschema2pojo:generate
     * */

    @Test(dataProvider = "correctTextsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkCorrectTexts(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .setMethod(Method.POST)
                        .buildRequest()
                        .sendRequest());
        MatcherAssert.assertThat("API reported errors in correct text: " + result, result.isEmpty());
    }

    @Test(dataProvider = "misspelledTextsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkMisspelledTexts(Language language, String text) {
        YandexSpellerAnswer result = getTheOnlyAnswer(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendRequest());
        MatcherAssert.assertThat("API failed to find spelling error in text: " + text,
                result, hasProperty(AnswerField.CODE.name, is(ERROR_UNKNOWN_WORD.code)));
    }

    @Test
    public void checkErrorCodeForMisspelling() {
        List<YandexSpellerAnswer> answers = getAnswers(
                requestBuilder()
                        .setLanguage(RUSSIAN)
                        .setText(RUS_MISSPELLED)
                        .buildRequest()
                        .sendRequest());
        MatcherAssert.assertThat("API displays wrong error code: " + answers.get(0).getCode() + " instead of: "
                + ERROR_UNKNOWN_WORD.code, answers.get(0).getCode() == ERROR_UNKNOWN_WORD.code);
    }

    @Test(dataProvider = "textsWithDigitsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIncorrectTextsWithDigits(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendRequest());
        MatcherAssert.assertThat("API failed to find error in text with digits: " + text,
                result.contains(text));
    }

    @Test(dataProvider = "textsWithDigitsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIgnoreDigitsOption(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .setOptions(IGNORE_DIGITS)
                        .buildRequest()
                        .sendRequest());
        MatcherAssert.assertThat("API reported errors in text with digits despite 'ignore digits' option: " + result,
                result.isEmpty());
    }

    // #5
    @Test(dataProvider = "textsWithLinksProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIncorrectTextsWithLinks(Language language, String text) {
        YandexSpellerAnswer result = getTheOnlyAnswer(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendRequest());
        MatcherAssert.assertThat("API failed to find error in text",
                result,
                allOf((hasProperty(AnswerField.CODE.name, is(ERROR_UNKNOWN_WORD.code))),
                        hasProperty(AnswerField.SUGGEST.name, not(emptyArray()))));
    }

    @Test(dataProvider = "textsWithLinksProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIgnoreUrlsOption(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .setOptions(IGNORE_URLS)
                        .buildRequest()
                        .sendRequest());
        MatcherAssert.assertThat("API reported errors in text with URL despite 'ignore URLs' option: "
                + result, result.isEmpty());
    }


    // capitalization doesn't work correctly. Just doesn't recognise errors like: moscow, london.
    @Test(dataProvider = "properNamesWithLowerCaseProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkIncorrectProperNamesWithLowerCase(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendRequest());
        MatcherAssert.assertThat("API failed to find error in proper name with lower case: " + text,
                result.contains(text));
    }

    // strange behavior for option that differ from described(here - 200).
    // on some text it fails, on other - not.
    // expect, that test fails
    @Test
    public void checkIncorrectOption() {
        requestBuilder()
                .setLanguage(ENGLISH)
                .setText(ENG_INCORRECT_FOR_INCORRECT_OPTION)
                .setOptions(INCORRECT_OPTION)
                .buildRequest()
                .sendRequest()
                .then().assertThat()
                .spec(badResponseSpecification());
    }

    @Test
    public void checkIncorrectLanguageParameter() {
        requestBuilder()
                .setLanguage(INCORRECT_LANGUAGE)
                .setText(ENG_CORRECT)
                .buildRequest()
                .sendRequest()
                .then().assertThat()
                .spec(badResponseSpecification())
                .body(containsString("SpellerService: Invalid parameter 'lang'"));
    }

    @Test
    public void checkCorrectFormatOption() {
        requestBuilder()
                .setLanguage(ENGLISH)
                .setText(ENG_CORRECT)
                .setFormat(HTML)
                .buildRequest()
                .sendRequest()
                .then().assertThat()
                .spec(goodResponseSpecification());
    }

    @Test
    public void checkIncorrectFormatOption() {
        requestBuilder()
                .setLanguage(ENGLISH)
                .setText(ENG_CORRECT)
                .setFormat(INCORRECT_FORMAT)
                .buildRequest()
                .sendRequest()
                .then().assertThat()
                .spec(badResponseSpecification())
                .and()
                .body(containsString("SpellerService: Invalid parameter 'format'"));
    }

    // russian test failed. In response - empty JSON. Other languages work fine
    @Test(dataProvider = "multiTextsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkTextsTest(Language language, String[] text) {
        Response yaResponse = requestBuilder()
                .setLanguage(language)
                .setText(text)
                .buildRequest()
                .sendCheckTextsRequest();

        List<String> result = getStringResultArray(yaResponse);
        MatcherAssert.assertThat("API failed to give suggestions with correct words: " + text,
                result.containsAll(Arrays.asList(text)));

        List<String> resultSuggestions = getStringSuggestionsArray(yaResponse);
        String[] expectedSuggestions = {ENG_MISSPELLED_CORRECTION, ENG_CORRECT, RUS_CORRECT, UKR_CORRECT};
        MatcherAssert.assertThat("No expected suggestions in text: " + text,
                resultSuggestions.stream().anyMatch(el -> Arrays.asList(expectedSuggestions).contains(el)));
    }

}