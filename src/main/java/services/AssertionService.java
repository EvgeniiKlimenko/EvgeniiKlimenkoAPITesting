package services;

import constants.AnswerField;
import io.restassured.response.Response;
import model.YandexSpellerAnswer;
import java.util.Arrays;
import java.util.List;

import static constants.SpellerErrorCode.ERROR_UNKNOWN_WORD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static services.YandexSpellerApiService.badResponseSpecification;
import static services.YandexSpellerApiService.goodResponseSpecification;

public class AssertionService {

    public void resultShouldBeEmpty(List<String> result) {
        assertThat("API reported errors in correct text: " + result, result.isEmpty());
    }

    public void checkCorrectErrorCodeOnMisspelling(YandexSpellerAnswer result, String text) {
        assertThat("API failed to find spelling error in text: " + text,
                result, hasProperty(AnswerField.CODE.name, is(ERROR_UNKNOWN_WORD.code)));
    }

    public void wordWithDigitsFound(List<String> result, String text) {
        assertThat("API failed to find errors in text with digits: " + text,
                result.contains(text));
    }

    public void wordWithDigitsIgnored(List<String> result, String text) {
        assertThat("API reported errors in text with digits despite 'ignore digits' option: " + result,
                result.isEmpty());
    }

    public void incorrectTextsWithLinksFound(YandexSpellerAnswer result) {
        assertThat("API failed to find error in text",
                result,
                allOf((hasProperty(AnswerField.CODE.name, is(ERROR_UNKNOWN_WORD.code))),
                        hasProperty(AnswerField.SUGGEST.name, not(emptyArray()))));
    }

    // capitalization doesn't work correctly. Just doesn't recognise errors like: moscow, london.
    public void textWithLowerCaseFound(List<String> result, String text) {
        assertThat("API failed to find error in proper name with lower case: " + text,
                result.contains(text));
    }

    public void correctWordsInSuggestions(List<String> resultSuggestions, String[] expectedSuggestions, String[] text) {
        assertThat("No expected suggestions in text: " + text,
                resultSuggestions.stream().anyMatch(el -> Arrays.asList(expectedSuggestions).contains(el)));
    }

    public void resultContainsSentText(List<String> result, String[] text) {
        assertThat("API failed to give suggestions with correct words: " + text,
                result.containsAll(Arrays.asList(text)));
    }

    public void shouldFindIncorrectFormat(Response yaResponse) {
        yaResponse.then().assertThat()
                .spec(badResponseSpecification())
                .and()
                .body(containsString("SpellerService: Invalid parameter 'format'"));
    }

    public void checkCorrectFormat(Response yaResponse) {
        yaResponse.then().assertThat().spec(goodResponseSpecification());
    }

    public void checkFindIncorrectLanguageOption(Response yaResponse) {
        yaResponse.then().assertThat()
                .spec(badResponseSpecification())
                .body(containsString("SpellerService: Invalid parameter 'lang'"));
    }

    public void incorrectOptionValueFound(Response yaResponse) {
        yaResponse.then().assertThat()
                .spec(badResponseSpecification());
    }

}
