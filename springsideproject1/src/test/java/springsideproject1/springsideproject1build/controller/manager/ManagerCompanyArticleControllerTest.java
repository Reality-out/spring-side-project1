package springsideproject1.springsideproject1build.controller.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import springsideproject1.springsideproject1build.domain.article.CompanyArticle;
import springsideproject1.springsideproject1build.domain.article.CompanyArticleDto;
import springsideproject1.springsideproject1build.service.CompanyArticleService;
import springsideproject1.springsideproject1build.utility.test.CompanyArticleTestUtility;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static springsideproject1.springsideproject1build.config.constant.LAYOUT_CONFIG.*;
import static springsideproject1.springsideproject1build.config.constant.REQUEST_URL_CONFIG.*;
import static springsideproject1.springsideproject1build.config.constant.VIEW_NAME_CONFIG.*;
import static springsideproject1.springsideproject1build.utility.ConstantUtils.*;
import static springsideproject1.springsideproject1build.utility.MainUtils.decodeUTF8;
import static springsideproject1.springsideproject1build.utility.MainUtils.encodeUTF8;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ManagerCompanyArticleControllerTest implements CompanyArticleTestUtility {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    CompanyArticleService articleService;

    private final JdbcTemplate jdbcTemplateTest;
    private final String dataTypeKorValue = "기사";
    private final String keyValue = "제목";

    @Autowired
    public ManagerCompanyArticleControllerTest(DataSource dataSource) {
        jdbcTemplateTest = new JdbcTemplate(dataSource);
    }

    @BeforeEach
    public void beforeEach() {
        resetTable(jdbcTemplateTest, companyArticleTable, true);
    }

    @DisplayName("기업 기사 추가 페이지 접속")
    @Test
    public void accessCompanyArticleAdd() throws Exception {
        mockMvc.perform(get(ADD_SINGLE_COMPANY_ARTICLE_URL))
                .andExpectAll(status().isOk(),
                        view().name(ADD_COMPANY_ARTICLE_VIEW + VIEW_SINGLE_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attributeExists(ARTICLE));
    }

    @DisplayName("기업 기사 추가 완료 페이지 접속")
    @Test
    public void accessCompanyArticleAddFinish() throws Exception {
        // given
        CompanyArticle article = createTestArticle();

        // then
        mockMvc.perform(processPostWithCompanyArticle(ADD_SINGLE_COMPANY_ARTICLE_URL, article))
                .andExpectAll(status().isSeeOther(),
                        redirectedUrlPattern(ADD_SINGLE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX + ALL_QUERY_STRING),
                        model().attribute(NAME, encodeUTF8(article.getName())));

        mockMvc.perform(processGetWithSingleParam(ADD_SINGLE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX,
                        NAME, encodeUTF8(article.getName())))
                .andExpectAll(status().isOk(),
                        view().name(MANAGER_ADD_VIEW + VIEW_SINGLE_FINISH_SUFFIX),
                        model().attribute(LAYOUT_PATH, ADD_FINISH_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attribute(KEY, keyValue),
                        model().attribute(VALUE, article.getName()));
    }

    @DisplayName("공백에 대한 기업 기사 유효성 검증")
    @Test
    public void validateSpaceCompanyArticleAdd() throws Exception {
        // given
        CompanyArticleDto articleDto = createTestArticle().toCompanyArticleDto();
        articleDto.setName(" ");
        articleDto.setPress(" ");
        articleDto.setSubjectCompany(" ");
        articleDto.setLink(" ");

        // then
        assertThat(mockMvc.perform(processPostWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto))
                .andExpectAll(view().name(ADD_COMPANY_ARTICLE_VIEW + VIEW_SINGLE_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue))
                .andReturn().getModelAndView().getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("null에 대한 기업 기사 유효성 검증")
    @Test
    public void validateNullCompanyArticleAdd() throws Exception {
        assertThat(mockMvc.perform(processPostWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, new CompanyArticleDto()))
                .andExpectAll(view().name(ADD_COMPANY_ARTICLE_VIEW + VIEW_SINGLE_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attributeExists(ARTICLE))
                .andReturn().getModelAndView().getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(new CompanyArticleDto());
    }

    @DisplayName("URL에 대한 기업 기사 유효성 검증")
    @Test
    public void validateURLCompanyArticleAdd() throws Exception {
        // given
        CompanyArticleDto articleDto = createTestArticle().toCompanyArticleDto();
        articleDto.setLink("NotUrl");

        // then
        assertThat(mockMvc.perform(processPostWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto))
                .andExpectAll(view().name(ADD_COMPANY_ARTICLE_VIEW + VIEW_SINGLE_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attributeExists(ARTICLE))
                .andReturn().getModelAndView().getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("Range에 대한 기업 기사 유효성 검증")
    @Test
    public void validateRangeCompanyArticleAdd() throws Exception {
        // given
        CompanyArticleDto articleDto = createTestArticle().toCompanyArticleDto();
        articleDto.setYear(1950);
        articleDto.setMonth(1);
        articleDto.setDate(1);

        // then
        assertThat(mockMvc.perform(processPostWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto))
                .andExpectAll(view().name(ADD_COMPANY_ARTICLE_VIEW + VIEW_SINGLE_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attributeExists(ARTICLE))
                .andReturn().getModelAndView().getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("Restrict에 대한 기업 기사 유효성 검증")
    @Test
    public void validateRestrictCompanyArticleAdd() throws Exception {
        // given
        CompanyArticleDto articleDto = createTestArticle().toCompanyArticleDto();
        articleDto.setImportance(3);

        // then
        assertThat(mockMvc.perform(processPostWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto))
                .andExpectAll(view().name(ADD_COMPANY_ARTICLE_VIEW + VIEW_SINGLE_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attributeExists(ARTICLE))
                .andReturn().getModelAndView().getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("TypeButInvalid에 대한 기업 기사 유효성 검증")
    @Test
    public void validateTypeButInvalidCompanyArticleAdd() throws Exception {
        // given
        CompanyArticleDto articleDto = createTestArticle().toCompanyArticleDto();
        articleDto.setYear(2000);
        articleDto.setMonth(2);
        articleDto.setDate(31);

        // then
        assertThat(mockMvc.perform(processPostWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto))
                .andExpectAll(view().name(ADD_COMPANY_ARTICLE_VIEW + VIEW_SINGLE_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attributeExists(ARTICLE))
                .andReturn().getModelAndView().getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("typeMismatch에 대한 기업 기사 유효성 검증")
    @Test
    public void validateTypeMismatchCompanyArticleAdd() throws Exception {
        // given
        CompanyArticleDto articleDto = createTestArticle().toCompanyArticleDto();
        articleDto.setPress(INVALID_VALUE);

        // then
        mockMvc.perform(processPostWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto)
                        .param("year", INVALID_VALUE)
                        .param("month", INVALID_VALUE)
                        .param("date", INVALID_VALUE)
                        .param("importance", INVALID_VALUE))
                .andExpectAll(view().name(ADD_COMPANY_ARTICLE_VIEW + VIEW_SINGLE_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attributeExists(ARTICLE));
    }

    @DisplayName("문자열을 사용하는 기업 기사들 추가 페이지 접속")
    @Test
    public void accessCompanyArticleAddWithString() throws Exception {
        mockMvc.perform(get(ADD_COMPANY_ARTICLE_WITH_STRING_URL))
                .andExpectAll(status().isOk(),
                        view().name(ADD_COMPANY_ARTICLE_VIEW + "multipleStringProcessPage"),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue));
    }

    @DisplayName("문자열을 사용하는 기업 기사들 추가 완료 페이지 접속")
    @Test
    public void accessCompanyArticleAddWithStringFinish() throws Exception {
        // given
        List<String> articleString = createTestStringArticle();
        List<String> nameList = articleService.registerArticlesWithString(
                articleString.getFirst(), articleString.get(1), articleString.getLast())
                .stream().map(CompanyArticle::getName).collect(Collectors.toList());
        articleService.removeArticle(createTestEqualDateArticle().getName());
        articleService.removeArticle(createTestNewArticle().getName());

        // then
        String nameListForURL = toStringForUrl(nameList);
        String nameListString = "nameList";

        assertThat(mockMvc.perform(processPostWithMultipleParams(ADD_COMPANY_ARTICLE_WITH_STRING_URL, new HashMap<>(){{
                    put("subjectCompany", articleString.getFirst());
                    put("articleString", articleString.get(1));
                    put("linkString", articleString.getLast());
                }}))
                .andExpectAll(status().isSeeOther(),
                        redirectedUrlPattern(ADD_COMPANY_ARTICLE_WITH_STRING_URL + URL_FINISH_SUFFIX + ALL_QUERY_STRING))
                .andReturn().getModelAndView().getModelMap().get(nameListString))
                .usingRecursiveComparison()
                .isEqualTo(nameListForURL);

        assertThat(mockMvc.perform(processGetWithSingleParam(ADD_COMPANY_ARTICLE_WITH_STRING_URL + URL_FINISH_SUFFIX,
                        nameListString, nameListForURL))
                .andExpectAll(status().isOk(),
                        view().name(MANAGER_ADD_VIEW + "multipleFinishPage"),
                        model().attribute(LAYOUT_PATH, ADD_FINISH_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attribute(KEY, keyValue),
                        model().attribute(nameListString, decodeUTF8(nameList)))
                .andReturn().getModelAndView().getModelMap().get(nameListString))
                .usingRecursiveComparison()
                .isEqualTo(decodeUTF8(nameList));
    }

    @DisplayName("기업 기사 변경 페이지 접속")
    @Test
    public void accessCompanyArticleModify() throws Exception {
        mockMvc.perform(get(UPDATE_COMPANY_ARTICLE_URL))
                .andExpectAll(status().isOk(),
                        view().name(UPDATE_COMPANY_ARTICLE_VIEW + VIEW_BEFORE_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, UPDATE_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue));
    }

    @DisplayName("기업 기사 변경 페이지 내 이름 검색")
    @Test
    public void searchNameCompanyArticleModify() throws Exception {
        // given
        CompanyArticle article = createTestArticle();

        // when
        article = articleService.registerArticle(article);

        // then
        assertThat(mockMvc.perform(processPostWithSingleParam(UPDATE_COMPANY_ARTICLE_URL, "numberOrName", article.getName()))
                .andExpectAll(status().isOk(),
                        view().name(UPDATE_COMPANY_ARTICLE_VIEW + VIEW_AFTER_PROCESS_SUFFIX),
                        model().attribute(LAYOUT_PATH, UPDATE_PROCESS_PATH),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attribute("updateUrl", UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX))
                .andReturn().getModelAndView().getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(article.toCompanyArticleDto());
    }

    @DisplayName("기업 기사 변경 완료 페이지 접속")
    @Test
    public void accessCompanyArticleModifyFinish() throws Exception {
        // given
        CompanyArticle article = createTestArticle();

        // when
        article = articleService.registerArticle(article);

        // then
        mockMvc.perform(processPostWithCompanyArticle(UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX, article))
                .andExpectAll(status().isSeeOther(),
                        redirectedUrlPattern(UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX + ALL_QUERY_STRING),
                        model().attribute(NAME, encodeUTF8(article.getName())));

        mockMvc.perform(processGetWithSingleParam(UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX,
                        NAME, encodeUTF8(article.getName())))
                .andExpectAll(status().isOk(),
                        view().name(MANAGER_UPDATE_VIEW + VIEW_FINISH_SUFFIX),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attribute(KEY, keyValue),
                        model().attribute(VALUE, article.getName()));
    }

    @DisplayName("기업 기사들 보기 페이지 접속")
    @Test
    public void accessCompanyArticlesSee() throws Exception {
        // given
        List<CompanyArticle> articleList = articleService.registerArticles(createTestArticle(), createTestNewArticle());

        // then
        assertThat(mockMvc.perform(get(SELECT_COMPANY_ARTICLE_URL))
                .andExpectAll(status().isOk(),
                        view().name(MANAGER_SELECT_VIEW + "companyArticlesPage"))
                .andReturn().getModelAndView().getModelMap().get("articles"))
                .usingRecursiveComparison()
                .isEqualTo(articleList);
    }

    @DisplayName("기업 기사 없애기 페이지 접속")
    @Test
    public void accessCompanyArticleRid() throws Exception {
        mockMvc.perform(get(REMOVE_COMPANY_ARTICLE_URL))
                .andExpectAll(status().isOk(),
                        view().name(MANAGER_REMOVE_VIEW + VIEW_PROCESS_SUFFIX),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attribute(DATA_TYPE_ENGLISH, ARTICLE),
                        model().attribute(REMOVE_KEY, NAME));
    }

    @DisplayName("기업 기사 없애기 완료 페이지 접속")
    @Test
    public void accessCompanyArticleRidFinish() throws Exception {
        // given
        CompanyArticle article = createTestArticle();
        String name = article.getName();

        // when
        articleService.registerArticle(article);

        // then
        mockMvc.perform(processPostWithSingleParam(REMOVE_COMPANY_ARTICLE_URL, NAME, name))
                .andExpectAll(status().isSeeOther(),
                        redirectedUrlPattern(REMOVE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX + ALL_QUERY_STRING),
                        model().attribute(NAME, encodeUTF8(name)));

        mockMvc.perform(processGetWithSingleParam(REMOVE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX, NAME, encodeUTF8(name)))
                .andExpectAll(status().isOk(),
                        view().name(MANAGER_REMOVE_VIEW + VIEW_FINISH_SUFFIX),
                        model().attribute(DATA_TYPE_KOREAN, dataTypeKorValue),
                        model().attribute(KEY, keyValue),
                        model().attribute(VALUE, name));
    }
}