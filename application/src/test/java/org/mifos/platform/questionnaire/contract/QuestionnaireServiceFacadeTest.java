/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 *  explanation of the license and how it is applied.
 */
package org.mifos.platform.questionnaire.contract;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.platform.questionnaire.QuestionnaireConstants;
import org.mifos.platform.questionnaire.QuestionnaireServiceFacadeImpl;
import org.mifos.platform.questionnaire.matchers.EventSourceMatcher;
import org.mifos.platform.questionnaire.matchers.EventSourcesMatcher;
import org.mifos.platform.questionnaire.matchers.QuestionDetailMatcher;
import org.mifos.platform.questionnaire.matchers.QuestionGroupDetailMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.mifos.platform.questionnaire.contract.QuestionType.DATE;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QuestionnaireServiceFacadeTest {

    private QuestionnaireServiceFacade questionnaireServiceFacade;

    @Mock
    private QuestionnaireService questionnaireService;

    private static final String TITLE = "Title";

    @Before
    public void setUp() throws Exception {
        questionnaireServiceFacade = new QuestionnaireServiceFacadeImpl(questionnaireService);
    }

    @Test
    public void shouldCreateQuestionGroup() throws ApplicationException {
        QuestionGroupDetail questionGroupDetail = getQuestionGroupDetail(TITLE, "Create", "Client", asList(getSectionDetailWithQuestions("S1", 123), getSectionDetailWithQuestions("S2", 123)));
        questionnaireServiceFacade.createQuestionGroup(questionGroupDetail);
        verify(questionnaireService, times(1)).defineQuestionGroup(argThat(
                new QuestionGroupDetailMatcher(questionGroupDetail)));
    }

    @Test
    public void testShouldCreateQuestions() throws ApplicationException {
        String title = TITLE + System.currentTimeMillis();
        String title1 = title + 1;
        String title2 = title + 2;
        questionnaireServiceFacade.createQuestions(asList(getQuestionDetail(0, title1, title1, QuestionType.FREETEXT),
                getQuestionDetail(0, title2, title2, DATE),
                getQuestionDetail(0, title2, title2, QuestionType.MULTI_SELECT,asList("choice1","choice2"))));
        verify(questionnaireService, times(1)).defineQuestion(argThat(new QuestionDetailMatcher(getQuestionDetail(0, title1, title1, QuestionType.FREETEXT))));
        verify(questionnaireService, times(1)).defineQuestion(argThat(new QuestionDetailMatcher(getQuestionDetail(0, title2, title2, DATE))));
        verify(questionnaireService, times(1)).defineQuestion(argThat(new QuestionDetailMatcher(getQuestionDetail(0, title2, title2, QuestionType.MULTI_SELECT, asList("choice1","choice2")))));
    }

    @Test
    public void testShouldCheckDuplicates() {
        questionnaireServiceFacade.isDuplicateQuestion(TITLE);
        verify(questionnaireService).isDuplicateQuestion(any(QuestionDefinition.class));
    }

    @Test
    public void testGetAllQuestion() {
        when(questionnaireService.getAllQuestions()).thenReturn(asList(getQuestionDetail(1, "title", "title", QuestionType.NUMERIC)));
        List<QuestionDetail> questionDetailList = questionnaireServiceFacade.getAllQuestions();
        assertNotNull(questionDetailList);
        assertThat(questionDetailList.get(0).getTitle(), is("title"));
        assertThat(questionDetailList.get(0).getId(), is(1));
        verify(questionnaireService).getAllQuestions();
    }

    @Test
    public void testGetAllQuestionGroups() {
        when(questionnaireService.getAllQuestionGroups()).thenReturn(
                asList(new QuestionGroupDetail(1, "title1", asList(getSectionDefinition("S1"), getSectionDefinition("S2"))),
                        new QuestionGroupDetail(2, "title2", asList(getSectionDefinition("S3")))));
        List<QuestionGroupDetail> questionGroupDetails = questionnaireServiceFacade.getAllQuestionGroups();
        assertNotNull(questionGroupDetails);

        QuestionGroupDetail questionGroupDetail1 = questionGroupDetails.get(0);
        assertThat(questionGroupDetail1.getId(), is(1));
        assertThat(questionGroupDetail1.getTitle(), is("title1"));

        QuestionGroupDetail questionGroupDetail2 = questionGroupDetails.get(1);
        assertThat(questionGroupDetail2.getId(), is(2));
        assertThat(questionGroupDetail2.getTitle(), is("title2"));

        verify(questionnaireService).getAllQuestionGroups();
    }

    @Test
    public void testGetQuestionGroupById() throws ApplicationException {
        int questionGroupId = 1;
        List<SectionDetail> sections = asList(getSectionDefinitionWithQuestions("S1", 121), getSectionDefinitionWithQuestions("S2", 122, 123));
        QuestionGroupDetail expectedQuestionGroupDetail = getQuestionGroupDetail(TITLE, "Create", "Client", sections);
        when(questionnaireService.getQuestionGroup(questionGroupId)).thenReturn(expectedQuestionGroupDetail);
        QuestionGroupDetail questionGroupDetail = questionnaireServiceFacade.getQuestionGroupDetail(questionGroupId);
        assertThat(questionGroupDetail, new QuestionGroupDetailMatcher(expectedQuestionGroupDetail));
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void testGetQuestionGroupByIdFailure() throws ApplicationException {
        int questionGroupId = 1;
        when(questionnaireService.getQuestionGroup(questionGroupId)).thenThrow(new ApplicationException(QuestionnaireConstants.QUESTION_GROUP_NOT_FOUND));
        try {
            questionnaireServiceFacade.getQuestionGroupDetail(questionGroupId);
        } catch (ApplicationException e) {
            verify(questionnaireService, times(1)).getQuestionGroup(questionGroupId);
            assertThat(e.getKey(), is(QuestionnaireConstants.QUESTION_GROUP_NOT_FOUND));
        }
    }

    @Test
    public void testGetQuestionById() throws ApplicationException {
        int questionId = 1;
        String title = "Title";
        when(questionnaireService.getQuestion(questionId)).thenReturn(new QuestionDetail(questionId, title, title, QuestionType.NUMERIC));
        QuestionDetail questionDetail = questionnaireServiceFacade.getQuestionDetail(questionId);
        assertNotNull("Question group should not be null", questionDetail);
        assertThat(questionDetail.getShortName(), is(title));
        assertThat(questionDetail.getType(), is(QuestionType.NUMERIC));
        verify(questionnaireService).getQuestion(questionId);
    }

    @Test
    public void testGetQuestionWithAnswerChoicesById() throws ApplicationException {
        int questionId = 1;
        String title = "Title";
        when(questionnaireService.getQuestion(questionId)).thenReturn(new QuestionDetail(questionId, title, title, QuestionType.MULTI_SELECT, asList("choice1","choice2")));
        QuestionDetail questionDetail = questionnaireServiceFacade.getQuestionDetail(questionId);
        assertNotNull("Question group should not be null", questionDetail);
        assertThat(questionDetail, new QuestionDetailMatcher(new QuestionDetail(questionId, title, title, QuestionType.MULTI_SELECT, asList("choice1","choice2"))));
        verify(questionnaireService).getQuestion(questionId);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void testGetQuestionByIdFailure() throws ApplicationException {
        int questionId = 1;
        when(questionnaireService.getQuestion(questionId)).thenThrow(new ApplicationException(QuestionnaireConstants.QUESTION_NOT_FOUND));
        try {
            questionnaireServiceFacade.getQuestionDetail(questionId);
        } catch (ApplicationException e) {
            verify(questionnaireService, times(1)).getQuestion(questionId);
            assertThat(e.getKey(), is(QuestionnaireConstants.QUESTION_NOT_FOUND));
        }
    }

    @Test
    public void testRetrieveEventSources() {
        EventSource event1 = makeEvent("Create", "Client", "Create Client");
        EventSource event2 = makeEvent("View", "Client", "View Client");
        List<EventSource> events = getEvents(event1, event2);
        when(questionnaireService.getAllEventSources()).thenReturn(events);
        List<EventSource> eventSources = questionnaireServiceFacade.getAllEventSources();
        assertNotNull(eventSources);
        assertTrue(eventSources.size() == 2);
        assertThat(eventSources, new EventSourcesMatcher(asList(event1, event2)));
        verify(questionnaireService).getAllEventSources();
    }

    @Test
    public void shouldRetrieveQuestionGroupsByEventSource() throws ApplicationException {
        QuestionGroupDetail questionGroupDetail = getQuestionGroupDetail(TITLE + 1, "Create", "Client", asList(getSectionDetailWithQuestions("Section1", 11, 22, 33)));
        when(questionnaireService.getQuestionGroups(any(EventSource.class))).thenReturn(asList(questionGroupDetail));
        List<QuestionGroupDetail> questionGroupDetails = questionnaireServiceFacade.getQuestionGroups("Create", "Client");
        assertThat(questionGroupDetails, is(notNullValue()));
        assertThat(questionGroupDetails.size(), is(1));
        QuestionGroupDetail questionGroupDetail1 = questionGroupDetails.get(0);
        assertThat(questionGroupDetail1.getId(), is(1));
        assertThat(questionGroupDetail1.getTitle(), is(TITLE + 1));
        List<SectionDetail> sections = questionGroupDetail1.getSectionDetails();
        assertThat(sections, is(notNullValue()));
        assertThat(sections.size(), is(1));
        SectionDetail section1 = sections.get(0);
        assertThat(section1.getName(), is("Section1"));
        List<SectionQuestionDetail> questions1 = section1.getQuestions();
        assertThat(questions1, is(notNullValue()));
        assertThat(questions1.size(), is(3));
        SectionQuestionDetail question1 = questions1.get(0);
        assertThat(question1.getQuestionId(), is(11));
        assertThat(question1.getTitle(), is("Q11"));
        assertThat(question1.isMandatory(), is(false));
        assertThat(question1.getQuestionType(), is(DATE));
        verify(questionnaireService, times(1)).getQuestionGroups(argThat(new EventSourceMatcher("Create", "Client", "Create.Client")));
    }

    private SectionDetail getSectionDetailWithQuestions(String name, int... questionIds) {
        SectionDetail sectionDetail = new SectionDetail();
        sectionDetail.setName(name);
        List<SectionQuestionDetail> questions = new ArrayList<SectionQuestionDetail>();
        for (int quesId : questionIds) {
            questions.add(new SectionQuestionDetail(quesId, "Q" + quesId, false, DATE));
        }
        sectionDetail.setQuestionDetails(questions);
        return sectionDetail;
    }

    private QuestionGroupDetail getQuestionGroupDetail(String title, String event, String source, List<SectionDetail> sections) {
        return new QuestionGroupDetail(1, title, new EventSource(event, source, null), sections);
    }

    private SectionDetail getSectionDefinition(String name) {
        SectionDetail sectionDetail = new SectionDetail();
        sectionDetail.setName(name);
        sectionDetail.addQuestion(new SectionQuestionDetail(123, "Q1", true, QuestionType.FREETEXT));
        return sectionDetail;
    }

    private SectionDetail getSectionDefinitionWithQuestions(String name, int... questionIds) {
        SectionDetail sectionDetail = new SectionDetail();
        sectionDetail.setName(name);
        for (int quesId : questionIds) {
            sectionDetail.addQuestion(new SectionQuestionDetail(quesId, "Q" + quesId, false, QuestionType.FREETEXT));
        }
        return sectionDetail;
    }

    private List<EventSource> getEvents(EventSource... event) {
        return Arrays.asList(event);
    }

    private EventSource makeEvent(String event, String source, String description) {
        return new EventSource(event, source, description);
    }

    private QuestionDetail getQuestionDetail(int id, String text, String shortName, QuestionType questionType) {
        return new QuestionDetail(id, text, shortName, questionType);
    }

    private QuestionDetail getQuestionDetail(int id, String text, String shortName, QuestionType questionType, List<String> choices) {
        return new QuestionDetail(id, text, shortName, questionType, choices);
    }

}

