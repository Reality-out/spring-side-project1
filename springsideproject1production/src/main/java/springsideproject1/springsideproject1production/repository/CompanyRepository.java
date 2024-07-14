package springsideproject1.springsideproject1production.repository;

import springsideproject1.springsideproject1production.domain.Company;

import java.util.Optional;

public interface CompanyRepository {
    /**
     * SELECT Company
     */
    Optional<Company> searchCompanyByCode(Long code);

    Optional<Company> searchCompanyByName(String name);

    /**
     * INSERT Company
     */
    void saveCompany(Company company);

    /**
     * REMOVE Company
     */
    void removeCompanyByCode(Long code);
}
