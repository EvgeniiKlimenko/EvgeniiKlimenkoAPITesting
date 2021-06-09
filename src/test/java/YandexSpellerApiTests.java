import io.restassured.response.Response;
import model.YandexSpellerAnswer;
import constants.Language;
import core.DataProvidersForSpeller;
import io.restassured.http.Method;
import org.testng.annotations.Test;
import services.AssertionService;
import services.YandexSpellerApiService;

import java.util.List;

import static constants.Option.*;
import static constants.Format.HTML;
import static constants.Format.INCORRECT_FORMAT;
import static constants.Language.*;
import static constants.Texts.*;
import static services.YandexSpellerApiService.*;

public class YandexSpellerApiTests {
    /*
     * To generate pojo from JSON with terminal: mvn jsonschema2pojo:generate
     * */

    private final AssertionService assertionService = new AssertionService();

    @Test(dataProvider = "correctTextsProvider",
            dataProviderClass = DataProvidersForSpeller.class) // +
    public void checkCorrectTexts(Language language, String text) {
        List<String> result = YandexSpellerApiService.getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .setMethod(Method.POST)
                        .buildRequest()
                        .sendRequest());
        assertionService.resultShouldBeEmpty(result);
    }

    @Test(dataProvider = "misspelledTextsProvider",
            dataProviderClass = DataProvidersForSpeller.class) // +
    public void checkMisspelledTexts(Language language, String text) {
        YandexSpellerAnswer result = getTheOnlyAnswer(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendRequest());

        assertionService.checkCorrectErrorCodeOnMisspelling(result, text);
    }

    @Test(dataProvider = "textsWithDigitsProvider",
            dataProviderClass = DataProvidersForSpeller.class) // +
    public void checkIncorrectTextsWithDigits(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendRequest());

        assertionService.wordWithDigitsFound(result, text);
    }

    @Test(dataProvider = "textsWithDigitsProvider",
            dataProviderClass = DataProvidersForSpeller.class) // +
    public void checkIgnoreDigitsOption(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .setOptions(IGNORE_DIGITS)
                        .buildRequest()
                        .sendRequest());

        assertionService.wordWithDigitsIgnored(result, text);
    }

    @Test(dataProvider = "textsWithLinksProvider",
            dataProviderClass = DataProvidersForSpeller.class) // +
    public void checkIncorrectTextsWithLinks(Language language, String text) {
        YandexSpellerAnswer result = getTheOnlyAnswer(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendRequest());

        assertionService.incorrectTextsWithLinksFound(result);
    }

    @Test(dataProvider = "textsWithLinksProvider",
            dataProviderClass = DataProvidersForSpeller.class) // +
    public void checkIgnoreUrlsOption(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .setOptions(IGNORE_URLS)
                        .buildRequest()
                        .sendRequest());
        assertionService.resultShouldBeEmpty(result);
    }


    @Test(dataProvider = "properNamesWithLowerCaseProvider",
            dataProviderClass = DataProvidersForSpeller.class) // +
    public void checkIncorrectProperNamesWithLowerCase(Language language, String text) {
        List<String> result = getStringResult(
                requestBuilder()
                        .setLanguage(language)
                        .setText(text)
                        .buildRequest()
                        .sendRequest());

        assertionService.textWithLowerCaseFound(result, text);
    }

    // strange behavior for option that differ from described(here - 200).
    // on some text it fails, on other - not.
    // expect, that test fails
    @Test
    public void checkIncorrectOption() {
        Response yaResponse = requestBuilder()
                .setLanguage(ENGLISH)
                .setText(ENG_INCORRECT_FOR_INCORRECT_OPTION)
                .setOptions(INCORRECT_OPTION)
                .buildRequest()
                .sendRequest();

        assertionService.incorrectOptionValueFound(yaResponse);
    }

    @Test
    public void checkIncorrectLanguageParameter() {
        Response yaResponse = requestBuilder()
                .setLanguage(INCORRECT_LANGUAGE)
                .setText(ENG_CORRECT)
                .buildRequest()
                .sendRequest();

        assertionService.checkFindIncorrectLanguageOption(yaResponse);
    }

    @Test
    public void checkCorrectFormatOption() {
        Response yaResponse = requestBuilder()
                .setLanguage(ENGLISH)
                .setText(ENG_CORRECT)
                .setFormat(HTML)
                .buildRequest()
                .sendRequest();

        assertionService.checkCorrectFormat(yaResponse);
    }

    @Test
    public void checkIncorrectFormatOption() {
        Response yaResponse = requestBuilder()
                .setLanguage(ENGLISH)
                .setText(ENG_CORRECT)
                .setFormat(INCORRECT_FORMAT)
                .buildRequest()
                .sendRequest();

        assertionService.shouldFindIncorrectFormat(yaResponse);
    }

    // russian test failed. In response - empty JSON. Other languages work fine
    @Test(dataProvider = "multiTextsProvider",
            dataProviderClass = DataProvidersForSpeller.class)
    public void checkTextsTest(Language language, String[] text) {
        Response yaResponse = YandexSpellerApiService.requestBuilder()
                .setLanguage(language)
                .setText(text)
                .buildRequest()
                .sendCheckTextsRequest();

        List<String> result = YandexSpellerApiService.getStringResultArray(yaResponse);
        List<String> resultSuggestions = YandexSpellerApiService.getStringSuggestionsArray(yaResponse);
        String[] expectedSuggestions = {ENG_MISSPELLED_CORRECTION, ENG_CORRECT, RUS_CORRECT, UKR_CORRECT};

        assertionService.resultContainsSentText(result, text);
        assertionService.correctWordsInSuggestions(resultSuggestions, expectedSuggestions, text);
    }

}