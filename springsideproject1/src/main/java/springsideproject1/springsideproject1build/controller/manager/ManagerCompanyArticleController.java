package springsideproject1.springsideproject1build.controller.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import springsideproject1.springsideproject1build.domain.CompanyArticle;
import springsideproject1.springsideproject1build.domain.CompanyArticleDto;
import springsideproject1.springsideproject1build.service.CompanyArticleService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static springsideproject1.springsideproject1build.config.constant.EXCEPTION_MESSAGE_CONFIG.NO_ARTICLE_WITH_THAT_NAME;
import static springsideproject1.springsideproject1build.config.constant.LAYOUT_CONFIG.*;
import static springsideproject1.springsideproject1build.config.constant.REQUEST_URL_CONFIG.*;
import static springsideproject1.springsideproject1build.config.constant.VIEW_NAME_CONFIG.*;
import static springsideproject1.springsideproject1build.utility.ConstantUtility.*;
import static springsideproject1.springsideproject1build.utility.MainUtility.decodeUTF8;
import static springsideproject1.springsideproject1build.utility.MainUtility.encodeUTF8;

@Controller
@RequiredArgsConstructor
public class ManagerCompanyArticleController {

    @Autowired
    private final CompanyArticleService articleService;
    private final String nameListString = "nameList";
    private final String dataTypeKorValue = "기사";
    private final String keyValue = "제목";

    /**
     * Add - Single
     */
    @GetMapping(ADD_SINGLE_COMPANY_ARTICLE_URL)
    @ResponseStatus(HttpStatus.OK)
    public String processAddSingleCompanyArticle(Model model) {
        model.addAttribute(LAYOUT_PATH, ADD_PROCESS_PATH);
        model.addAttribute(DATA_TYPE_KOREAN, dataTypeKorValue);
        model.addAttribute(ARTICLE, new CompanyArticleDto());
        return ADD_COMPANY_ARTICLE_VIEW + VIEW_SINGLE_PROCESS_SUFFIX;
    }

    @GetMapping(ADD_SINGLE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX)
    @ResponseStatus(HttpStatus.OK)
    public String finishAddSingleCompanyArticle(@RequestParam String name, Model model) {
        model.addAttribute(LAYOUT_PATH, ADD_FINISH_PATH);
        model.addAttribute(DATA_TYPE_KOREAN, dataTypeKorValue);
        model.addAttribute(KEY, keyValue);
        model.addAttribute(VALUE, decodeUTF8(name));
        return MANAGER_ADD_VIEW + VIEW_SINGLE_FINISH_SUFFIX;
    }

    @PostMapping(ADD_SINGLE_COMPANY_ARTICLE_URL)
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public String submitAddSingleCompanyArticle(RedirectAttributes redirect, @ModelAttribute CompanyArticleDto articleDto) {
        redirect.addAttribute(NAME, encodeUTF8(articleDto.getName()));
        articleService.joinArticle(CompanyArticle.builder().articleDto(articleDto).build());
        return URL_REDIRECT_PREFIX + ADD_SINGLE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX;
    }

    /**
     * Add - Multiple
     */
    @GetMapping(ADD_COMPANY_ARTICLE_WITH_STRING_URL)
    @ResponseStatus(HttpStatus.OK)
    public String processAddCompanyArticlesUsingString(Model model) {
        model.addAttribute(LAYOUT_PATH, ADD_PROCESS_PATH);
        model.addAttribute(DATA_TYPE_KOREAN, dataTypeKorValue);
        return ADD_COMPANY_ARTICLE_VIEW + "multipleStringProcessPage";
    }

    @GetMapping(ADD_COMPANY_ARTICLE_WITH_STRING_URL + URL_FINISH_SUFFIX)
    @ResponseStatus(HttpStatus.OK)
    public String finishAddCompanyArticlesUsingString(@RequestParam List<String> nameList, Model model) {
        model.addAttribute(LAYOUT_PATH, ADD_FINISH_PATH);
        model.addAttribute(DATA_TYPE_KOREAN, dataTypeKorValue);
        model.addAttribute(KEY, keyValue);
        model.addAttribute(nameListString, decodeUTF8(nameList));

        return MANAGER_ADD_VIEW + "multipleFinishPage";
    }

    @PostMapping(ADD_COMPANY_ARTICLE_WITH_STRING_URL)
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public String submitAddCompanyArticlesUsingString(RedirectAttributes redirect, @RequestParam String subjectCompany,
                                                      @RequestParam String articleString, @RequestParam String linkString) {
        redirect.addAttribute(nameListString, encodeUTF8(articleService.joinArticlesWithString(
                subjectCompany, articleString, linkString).stream().map(CompanyArticle::getName).collect(Collectors.toList())));
        return URL_REDIRECT_PREFIX + ADD_COMPANY_ARTICLE_WITH_STRING_URL + URL_FINISH_SUFFIX;
    }

    /**
     * Select
     */
    @GetMapping(SELECT_COMPANY_ARTICLE_URL)
    @ResponseStatus(HttpStatus.OK)
    public String selectCompanyArticle(Model model) {
        model.addAttribute(LAYOUT_PATH, SELECT_PATH);
        model.addAttribute("articles", articleService.findArticles());
        return MANAGER_SELECT_VIEW + "companyArticlesPage";
    }

    /**
     * Update
     */
    @GetMapping(UPDATE_COMPANY_ARTICLE_URL)
	@ResponseStatus(HttpStatus.OK)
	public String initiateUpdateCompanyArticle(Model model) {
        model.addAttribute(LAYOUT_PATH, UPDATE_PROCESS_PATH);
        model.addAttribute(DATA_TYPE_KOREAN, dataTypeKorValue);
		return UPDATE_COMPANY_ARTICLE_VIEW + VIEW_BEFORE_PROCESS_SUFFIX;
	}

    @PostMapping(UPDATE_COMPANY_ARTICLE_URL)
    @ResponseStatus(HttpStatus.OK)
    public String processUpdateCompanyArticle(Model model, @RequestParam String name) {
        Optional<CompanyArticle> articleOrEmpty = articleService.findArticleByName(name);
        if (articleOrEmpty.isEmpty()) {
            throw new IllegalStateException(NO_ARTICLE_WITH_THAT_NAME);
        } else {
            CompanyArticleDto article = articleOrEmpty.get().toCompanyArticleDto();
            model.addAttribute(LAYOUT_PATH, UPDATE_PROCESS_PATH);
            model.addAttribute(DATA_TYPE_KOREAN, dataTypeKorValue);
            model.addAttribute("updateUrl", UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX);
            model.addAttribute(ARTICLE, article);
        }
        return UPDATE_COMPANY_ARTICLE_VIEW + VIEW_AFTER_PROCESS_SUFFIX;
    }

    @PostMapping(UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX)
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public String submitUpdateCompanyArticle(RedirectAttributes redirect, @ModelAttribute CompanyArticleDto articleDto) {
        articleService.renewArticle(CompanyArticle.builder().articleDto(articleDto).build());
        redirect.addAttribute(NAME, encodeUTF8(articleDto.getName()));
        return URL_REDIRECT_PREFIX + UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX;
    }

    @GetMapping(UPDATE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX)
	@ResponseStatus(HttpStatus.OK)
	public String finishUpdateCompanyArticle(@RequestParam String name, Model model) {
        model.addAttribute(DATA_TYPE_KOREAN, dataTypeKorValue);
        model.addAttribute(KEY, keyValue);
        model.addAttribute(VALUE, decodeUTF8(name));

        return MANAGER_UPDATE_VIEW + VIEW_FINISH_SUFFIX;
	}

    /**
     * Remove
     */
    @GetMapping(REMOVE_COMPANY_ARTICLE_URL)
    @ResponseStatus(HttpStatus.OK)
    public String processRemoveCompanyArticle(Model model) {
        model.addAttribute(DATA_TYPE_KOREAN, dataTypeKorValue);
        model.addAttribute(DATA_TYPE_ENGLISH, ARTICLE);
        model.addAttribute(KEY, NAME);
        return MANAGER_REMOVE_VIEW + VIEW_PROCESS_SUFFIX;
    }

    @GetMapping(REMOVE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX)
    @ResponseStatus(HttpStatus.OK)
    public String finishRemoveCompanyArticle(@RequestParam String name, Model model) {
        model.addAttribute(DATA_TYPE_KOREAN, dataTypeKorValue);
        model.addAttribute(KEY, keyValue);
        model.addAttribute(VALUE, decodeUTF8(name));
        return MANAGER_REMOVE_VIEW + VIEW_FINISH_SUFFIX;
    }

    @PostMapping(REMOVE_COMPANY_ARTICLE_URL)
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public String submitRemoveCompanyArticle(RedirectAttributes redirect, @RequestParam String name) {
        articleService.removeArticle(name);
        redirect.addAttribute(NAME, encodeUTF8(name));
        return URL_REDIRECT_PREFIX + REMOVE_COMPANY_ARTICLE_URL + URL_FINISH_SUFFIX;
    }
}