package springsideproject1.springsideproject1build.domain.validator.article;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import springsideproject1.springsideproject1build.domain.entity.article.company.CompanyArticle;
import springsideproject1.springsideproject1build.domain.entity.article.company.CompanyArticleBufferSimple;
import springsideproject1.springsideproject1build.domain.entity.article.company.CompanyArticleDto;
import springsideproject1.springsideproject1build.domain.service.CompanyArticleService;
import springsideproject1.springsideproject1build.domain.service.CompanyService;
import springsideproject1.springsideproject1build.util.test.CompanyArticleTestUtils;
import springsideproject1.springsideproject1build.util.test.CompanyTestUtils;

import javax.sql.DataSource;
import java.util.HashMap;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static springsideproject1.springsideproject1build.domain.error.constant.EXCEPTION_STRING.*;
import static springsideproject1.springsideproject1build.domain.valueobject.CLASS.ARTICLE;
import static springsideproject1.springsideproject1build.domain.valueobject.CLASS.SUBJECT_COMPANY;
import static springsideproject1.springsideproject1build.domain.valueobject.DATABASE.TEST_COMPANY_ARTICLE_TABLE;
import static springsideproject1.springsideproject1build.domain.valueobject.DATABASE.TEST_COMPANY_TABLE;
import static springsideproject1.springsideproject1build.domain.valueobject.LAYOUT.*;
import static springsideproject1.springsideproject1build.domain.valueobject.REQUEST_URL.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CompanyArticleValidatorErrorTest implements CompanyArticleTestUtils, CompanyTestUtils {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    CompanyArticleService articleService;

    @Autowired
    CompanyService companyService;

    private final JdbcTemplate jdbcTemplateTest;

    @Autowired
    public CompanyArticleValidatorErrorTest(DataSource dataSource) {
        jdbcTemplateTest = new JdbcTemplate(dataSource);
    }

    @BeforeEach
    public void beforeEach() {
        resetTable(jdbcTemplateTest, TEST_COMPANY_ARTICLE_TABLE, true);
        resetTable(jdbcTemplateTest, TEST_COMPANY_TABLE);
    }

    @DisplayName("기사 입력일이 유효하지 않은 기업 기사 추가 유효성 검증")
    @Test
    public void invalidDateCompanyArticleAdd() throws Exception {
        // given & when
        CompanyArticleDto articleDto = createTestCompanyArticleDto();
        articleDto.setYear(2000);
        articleDto.setMonth(2);
        articleDto.setDays(31);

        // then
        assertThat(requireNonNull(mockMvc.perform(postWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto))
                .andExpectAll(view().name(addSingleArticleProcessPage),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(ERROR, (String) null))
                .andReturn().getModelAndView()).getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("중복 기사명을 사용하는 기업 기사 추가")
    @Test
    public void duplicatedNameCompanyArticleAdd() throws Exception {
        // given
        CompanyArticle article1 = testCompanyArticle;
        String commonName = article1.getName();
        CompanyArticleDto articleDto2 = createTestNewCompanyArticleDto();
        articleDto2.setName(commonName);

        // when
        articleService.registerArticle(article1);
        companyService.registerCompany(samsungElectronics);

        // then
        assertThat(requireNonNull(mockMvc.perform(postWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto2))
                .andExpectAll(view().name(addSingleArticleProcessPage),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(ERROR, (String) null))
                .andReturn().getModelAndView()).getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto2);
    }

    @DisplayName("중복 기사 링크를 사용하는 기업 기사 추가")
    @Test
    public void duplicatedLinkCompanyArticleAdd() throws Exception {
        // given
        CompanyArticle article1 = testCompanyArticle;
        String commonLink = article1.getLink();
        CompanyArticleDto articleDto2 = createTestNewCompanyArticleDto();
        articleDto2.setLink(commonLink);

        // when
        articleService.registerArticle(article1);
        companyService.registerCompany(samsungElectronics);

        // then
        assertThat(requireNonNull(mockMvc.perform(postWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto2))
                .andExpectAll(view().name(addSingleArticleProcessPage),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(ERROR, (String) null))
                .andReturn().getModelAndView()).getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto2);
    }

    @DisplayName("대상 기업이 추가되지 않은 기업 기사 추가")
    @Test
    public void notRegisteredSubjectCompanyArticleAdd() throws Exception {
        // given & when
        CompanyArticleDto articleDto = createTestCompanyArticleDto();

        // then
        assertThat(requireNonNull(mockMvc.perform(postWithCompanyArticleDto(ADD_SINGLE_COMPANY_ARTICLE_URL, articleDto))
                .andExpectAll(view().name(addSingleArticleProcessPage),
                        model().attribute(LAYOUT_PATH, ADD_PROCESS_PATH),
                        model().attribute(ERROR, (String) null))
                .andReturn().getModelAndView()).getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("기사 입력일이 유효하지 않은, 문자열을 사용하는 기업 기사들 추가")
    @Test
    public void invalidDateCompanyArticleAddWithString() throws Exception {
        // given
        CompanyArticleDto articleDto = createTestCompanyArticleDto();
        articleDto.setYear(2000);
        articleDto.setMonth(2);
        articleDto.setDays(31);
        CompanyArticleBufferSimple articleBuffer = CompanyArticleBufferSimple.builder().articleDto(articleDto).build();

        // when
        companyService.registerCompany(samsungElectronics);

        // then
        requireNonNull(mockMvc.perform(postWithMultipleParams(ADD_COMPANY_ARTICLE_WITH_STRING_URL, new HashMap<>() {{
                    put(nameDatePressString, articleBuffer.getNameDatePressString());
                    put(SUBJECT_COMPANY, articleBuffer.getSubjectCompany());
                    put(linkString, articleBuffer.getLinkString());
                }}))
                .andExpectAll(view().name(
                                URL_REDIRECT_PREFIX + ADD_COMPANY_ARTICLE_WITH_STRING_URL + URL_FINISH_SUFFIX),
                        model().attribute(IS_BEAN_VALIDATION_ERROR, String.valueOf(false)),
                        model().attribute(ERROR_SINGLE, (String) null)));
    }

    @DisplayName("중복 기사명을 사용하는, 문자열을 사용하는 기업 기사들 추가")
    @Test
    public void duplicatedNameCompanyArticleAddWithString() throws Exception {
        // given & when
        articleService.registerArticle(CompanyArticle.builder().article(testCompanyArticle).name(testEqualDateCompanyArticle.getName()).build());
        companyService.registerCompany(samsungElectronics);

        // then
        requireNonNull(mockMvc.perform(postWithMultipleParams(ADD_COMPANY_ARTICLE_WITH_STRING_URL, new HashMap<>() {{
                    put(nameDatePressString, testEqualDateCompanyArticleStringBuffer.getNameDatePressString());
                    put(SUBJECT_COMPANY, testEqualDateCompanyArticleStringBuffer.getSubjectCompany());
                    put(linkString, testEqualDateCompanyArticleStringBuffer.getLinkString());
                }}))
                .andExpectAll(view().name(
                                URL_REDIRECT_PREFIX + ADD_COMPANY_ARTICLE_WITH_STRING_URL + URL_FINISH_SUFFIX),
                        model().attribute(IS_BEAN_VALIDATION_ERROR, String.valueOf(false)),
                        model().attribute(ERROR_SINGLE, (String) null)));
    }

    @DisplayName("중복 기사 링크를 사용하는, 문자열을 사용하는 기업 기사들 추가")
    @Test
    public void duplicatedLinkCompanyArticleAddWithString() throws Exception {
        // given & when
        articleService.registerArticle(CompanyArticle.builder().article(testCompanyArticle).link(testEqualDateCompanyArticle.getLink()).build());
        companyService.registerCompany(samsungElectronics);

        // then
        requireNonNull(mockMvc.perform(postWithMultipleParams(ADD_COMPANY_ARTICLE_WITH_STRING_URL, new HashMap<>() {{
                    put(nameDatePressString, testEqualDateCompanyArticleStringBuffer.getNameDatePressString());
                    put(SUBJECT_COMPANY, testEqualDateCompanyArticleStringBuffer.getSubjectCompany());
                    put(linkString, testEqualDateCompanyArticleStringBuffer.getLinkString());
                }}))
                .andExpectAll(view().name(
                                URL_REDIRECT_PREFIX + ADD_COMPANY_ARTICLE_WITH_STRING_URL + URL_FINISH_SUFFIX),
                        model().attribute(IS_BEAN_VALIDATION_ERROR, String.valueOf(false)),
                        model().attribute(ERROR_SINGLE, (String) null)));
    }

    @DisplayName("기사 입력일이 유효하지 않은 기업 기사 변경")
    @Test
    public void invalidDateCompanyArticleModify() throws Exception {
        // given & when
        CompanyArticleDto articleDto = createTestCompanyArticleDto();
        articleDto.setYear(2000);
        articleDto.setMonth(2);
        articleDto.setDays(31);

        // then
        assertThat(requireNonNull(mockMvc.perform(postWithCompanyArticleDto(modifyArticleFinishUrl, articleDto))
                .andExpectAll(view().name(modifyArticleProcessPage),
                        model().attribute(LAYOUT_PATH, UPDATE_PROCESS_PATH),
                        model().attribute(ERROR, (String) null))
                .andReturn().getModelAndView()).getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("존재하지 않는 기업 기사명을 사용하는 기업 기사 변경")
    @Test
    public void notExistNameCompanyArticleModify() throws Exception {
        // given
        CompanyArticleDto articleDto = createTestCompanyArticleDto();
        articleService.registerArticle(testCompanyArticle);

        // when
        articleDto.setName(testNewCompanyArticle.getName());

        // then
        assertThat(requireNonNull(mockMvc.perform(postWithCompanyArticleDto(modifyArticleFinishUrl, articleDto))
                .andExpectAll(view().name(modifyArticleProcessPage),
                        model().attribute(LAYOUT_PATH, UPDATE_PROCESS_PATH),
                        model().attribute(ERROR, (String) null))
                .andReturn().getModelAndView()).getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("존재하지 않는 기업 링크를 사용하는 기업 기사 변경")
    @Test
    public void notExistLinkCompanyArticleModify() throws Exception {
        // given
        CompanyArticleDto articleDto = createTestCompanyArticleDto();
        articleService.registerArticle(testCompanyArticle);

        // when
        articleDto.setLink(testNewCompanyArticle.getLink());

        // then
        assertThat(requireNonNull(mockMvc.perform(postWithCompanyArticleDto(modifyArticleFinishUrl, articleDto))
                .andExpectAll(view().name(modifyArticleProcessPage),
                        model().attribute(LAYOUT_PATH, UPDATE_PROCESS_PATH),
                        model().attribute(ERROR, (String) null))
                .andReturn().getModelAndView()).getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }

    @DisplayName("대상 기업이 추가되지 않은 기업 기사 변경")
    @Test
    public void notRegisteredSubjectCompanyArticleModify() throws Exception {
        // given & when
        CompanyArticleDto articleDto = createTestCompanyArticleDto();

        // then
        assertThat(requireNonNull(mockMvc.perform(postWithCompanyArticleDto(modifyArticleFinishUrl, articleDto))
                .andExpectAll(view().name(modifyArticleProcessPage),
                        model().attribute(LAYOUT_PATH, UPDATE_PROCESS_PATH),
                        model().attribute(ERROR, (String) null))
                .andReturn().getModelAndView()).getModelMap().get(ARTICLE))
                .usingRecursiveComparison()
                .isEqualTo(articleDto);
    }
}
