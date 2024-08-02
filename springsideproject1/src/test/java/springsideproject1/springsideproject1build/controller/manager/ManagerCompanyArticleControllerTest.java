package springsideproject1.springsideproject1build.controller.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import springsideproject1.springsideproject1build.domain.CompanyArticle;
import springsideproject1.springsideproject1build.service.CompanyArticleService;
import springsideproject1.springsideproject1build.utility.test.CompanyArticleTest;

import javax.sql.DataSource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static springsideproject1.springsideproject1build.config.constant.LAYOUT_CONFIG.*;
import static springsideproject1.springsideproject1build.config.constant.REQUEST_URL_CONFIG.*;
import static springsideproject1.springsideproject1build.config.constant.VIEW_NAME_CONFIG.*;
import static springsideproject1.springsideproject1build.utility.MainUtility.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ManagerCompanyArticleControllerTest implements CompanyArticleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    CompanyArticleService articleService;

    private final JdbcTemplate jdbcTemplateTest;

    @Autowired
    public ManagerCompanyArticleControllerTest(DataSource dataSource) {
        jdbcTemplateTest = new JdbcTemplate(dataSource);
    }

    @BeforeEach
    public void beforeEach() {
        resetTable(jdbcTemplateTest, companyArticleTable, true);
    }

    @DisplayName("단일 기사 등록 페이지 접속")
    @Test
    public void accessArticleRegisterPage() throws Exception {
        mockMvc.perform(get(ADD_SINGLE_COMPANY_ARTICLE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(ADD_COMPANY_ARTICLE_VIEW + "singleProcessPage"))
                .andExpect(model().attribute("layoutPath", PROCESS_ADD_COMPANY_ARTICLE_PATH));
    }

    @DisplayName("단일 기사 등록 완료 페이지 접속")
    @Test
    public void accessArticleRegisterFinishPage() throws Exception {
        // given
        CompanyArticle article = createTestArticle();

        // then
        mockMvc.perform(post(ADD_SINGLE_COMPANY_ARTICLE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", article.getName())
                        .param("press", article.getPress())
                        .param("subjectCompany", article.getSubjectCompany())
                        .param("link", article.getLink())
                        .param("year", String.valueOf(article.getDate().getYear()))
                        .param("month", String.valueOf(article.getDate().getMonthValue()))
                        .param("date", String.valueOf(article.getDate().getDayOfMonth()))
                        .param("importance", String.valueOf(article.getImportance())))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrlPattern(ADD_SINGLE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX + "?*"))
                .andExpect(model().attribute("name", URLEncoder.encode(article.getName(), StandardCharsets.UTF_8)));

        mockMvc.perform(get(ADD_SINGLE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX)
                        .param("name", URLEncoder.encode(article.getName(), StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(view().name(ADD_COMPANY_ARTICLE_VIEW + "singleFinishPage"))
                .andExpect(model().attribute("layoutPath", FINISH_ADD_COMPANY_ARTICLE_PATH))
                .andExpect(model().attribute("name", article.getName()));
    }

    @DisplayName("단일 문자열을 사용하는 기사 등록 페이지 접속")
    @Test
    public void StringArticleRegisterPage() throws Exception {
        mockMvc.perform(get(ADD_COMPANY_ARTICLE_WITH_STRING_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(ADD_COMPANY_ARTICLE_VIEW + "multipleProcessStringPage"))
                .andExpect(model().attribute("layoutPath", PROCESS_ADD_COMPANY_ARTICLE_PATH));
    }

    @DisplayName("단일 문자열을 사용하는 기사 등록 완료 페이지 접속")
    @Test
    public void StringArticleRegisterFinishPage() throws Exception {
        // given
        List<String> articleString = createTestStringArticle();
        List<String> nameList = articleService.joinArticlesWithString(
                articleString.getFirst(), articleString.get(1), articleString.getLast())
                .stream().map(CompanyArticle::getName).collect(Collectors.toList());
        articleService.removeArticle(createTestEqualDateArticle().getName());
        articleService.removeArticle(createTestNewArticle().getName());

        // then
        String nameListForURL = toStringForUrl(encodeUTF8(nameList));
        assertThat(mockMvc.perform(post(ADD_COMPANY_ARTICLE_WITH_STRING_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("subjectCompany", articleString.getFirst())
                        .param("articleString", articleString.get(1))
                        .param("linkString", articleString.getLast()))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrlPattern(ADD_COMPANY_ARTICLE_WITH_STRING_URL + URL_FINISH_SUFFIX + "?*"))
                .andReturn().getModelAndView().getModelMap().get("nameList"))
                .usingRecursiveComparison()
                .isEqualTo(nameListForURL);

        assertThat(mockMvc.perform(get(ADD_COMPANY_ARTICLE_WITH_STRING_URL + URL_FINISH_SUFFIX)
                        .param("nameList", nameListForURL))
                .andExpect(status().isOk())
                .andExpect(view().name(ADD_COMPANY_ARTICLE_VIEW + "multipleFinishStringPage"))
                .andExpect(model().attribute("layoutPath", FINISH_ADD_COMPANY_ARTICLE_PATH))
                .andReturn().getModelAndView().getModelMap().get("nameList"))
                .usingRecursiveComparison()
                .isEqualTo(decodeUTF8(nameList));
    }

    @DisplayName("단일 기사 갱신 페이지 접속")
    @Test
    public void accessArticleUpdatePage() throws Exception {
        mockMvc.perform(get(UPDATE_COMPANY_ARTICLE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(UPDATE_COMPANY_ARTICLE_VIEW + "before" + VIEW_PASCAL_PROCESS_SUFFIX))
                .andExpect(model().attribute("layoutPath", PROCESS_UPDATE_COMPANY_ARTICLE_PATH));
    }

    @DisplayName("단일 기사 갱신 페이지 내 이름 검색")
    @Test
    public void searchNameInArticleUpdatePage() throws Exception {
        // given
        CompanyArticle article = createTestArticle();

        // when
        article = articleService.joinArticle(article);

        // then
        assertThat(mockMvc.perform(post(UPDATE_COMPANY_ARTICLE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", article.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name(UPDATE_COMPANY_ARTICLE_VIEW + "after" + VIEW_PASCAL_PROCESS_SUFFIX))
                .andExpect(model().attribute("layoutPath", PROCESS_UPDATE_COMPANY_ARTICLE_PATH))
                .andExpect(model().attribute("updateUrl", UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX))
                .andExpect(model().attribute("year", article.getDate().getYear()))
                .andExpect(model().attribute("month", article.getDate().getMonthValue()))
                .andExpect(model().attribute("date", article.getDate().getDayOfMonth()))
                .andReturn().getModelAndView().getModelMap().get("article"))
                .usingRecursiveComparison()
                .isEqualTo(article);
    }

    @DisplayName("단일 기사 갱신 완료 페이지 접속")
    @Test
    public void accessArticleUpdateFinishPage() throws Exception {
        // given
        CompanyArticle article = createTestArticle();

        // when
        article = articleService.joinArticle(article);

        // then
        mockMvc.perform(post(UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", article.getName())
                        .param("press", article.getPress())
                        .param("subjectCompany", article.getSubjectCompany())
                        .param("link", article.getLink())
                        .param("year", String.valueOf(article.getDate().getYear()))
                        .param("month", String.valueOf(article.getDate().getMonthValue()))
                        .param("date", String.valueOf(article.getDate().getDayOfMonth()))
                        .param("importance", String.valueOf(article.getImportance())))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrlPattern(UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX + "?*"))
                .andExpect(model().attribute("name", URLEncoder.encode(article.getName(), StandardCharsets.UTF_8)));

        mockMvc.perform(get(UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX)
                        .param("name", URLEncoder.encode(article.getName(), StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(view().name(UPDATE_COMPANY_ARTICLE_VIEW + VIEW_FINISH_SUFFIX))
                .andExpect(model().attribute("layoutPath", FINISH_UPDATE_COMPANY_ARTICLE_PATH))
                .andExpect(model().attribute("name", article.getName()));
    }

    @DisplayName("단일 기사 삭제 페이지 접속")
    @Test
    public void accessArticleRemovePage() throws Exception {
        mockMvc.perform(get(REMOVE_COMPANY_ARTICLE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(MANAGER_REMOVE_VIEW + VIEW_PROCESS_SUFFIX))
                .andExpect(model().attribute("dataTypeKor", "기사"))
                .andExpect(model().attribute("dataTypeEng", "article"))
                .andExpect(model().attribute("key", "name"));
    }

    @DisplayName("단일 기사 삭제 완료 페이지 접속")
    @Test
    public void accessArticleRemoveFinishPage() throws Exception {
        // given
        CompanyArticle article = createTestArticle();
        String name = article.getName();

        // when
        articleService.joinArticle(article);

        // then
        mockMvc.perform(post(REMOVE_COMPANY_ARTICLE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", name))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrlPattern(REMOVE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX + "?*"))
                .andExpect(model().attribute("name", URLEncoder.encode(name, StandardCharsets.UTF_8)));

        mockMvc.perform(get(REMOVE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX)
                        .param("name", URLEncoder.encode(name, StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(view().name(MANAGER_REMOVE_VIEW + VIEW_FINISH_SUFFIX))
                .andExpect(model().attribute("dataTypeKor", "기사"))
                .andExpect(model().attribute("key", "제목"))
                .andExpect(model().attribute("value", name));
    }
}