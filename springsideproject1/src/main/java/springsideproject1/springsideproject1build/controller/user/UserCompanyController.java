package springsideproject1.springsideproject1build.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import springsideproject1.springsideproject1build.service.CompanyService;

import static springsideproject1.springsideproject1build.Utility.isNumeric;
import static springsideproject1.springsideproject1build.config.constant.FOLDER_PATH_CONFIG.BASIC_LAYOUT_PATH;
import static springsideproject1.springsideproject1build.config.constant.VIEW_NAME_CONFIG.*;

@Controller
@RequestMapping("/company")
@RequiredArgsConstructor
public class UserCompanyController {

    @Autowired
    private final CompanyService companyService;

    /**
     * Main
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String companySubPage(Model model) {
        model.addAttribute("layoutPath", BASIC_LAYOUT_PATH);
        return USER_COMPANY_VIEW_NAME + VIEW_NAME_SUB_SUFFIX;
    }

    /**
     * Select
     */
    @GetMapping("/{nameOrCode}")
    @ResponseStatus(HttpStatus.OK)
    public String companyLookUp(
            @PathVariable String nameOrCode, Model model) {
        model.addAttribute("layoutPath", BASIC_LAYOUT_PATH);
        model.addAttribute("company", (isNumeric(nameOrCode)) ?
                companyService.SearchOneCompanyByCode(nameOrCode).get() :
                companyService.SearchOneCompanyByName(nameOrCode).get());
        return USER_COMPANY_VIEW_NAME + VIEW_NAME_SHOW_SUFFIX;
    }
}
