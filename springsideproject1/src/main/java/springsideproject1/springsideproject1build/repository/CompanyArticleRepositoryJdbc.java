package springsideproject1.springsideproject1build.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import springsideproject1.springsideproject1build.domain.CompanyArticle;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static springsideproject1.springsideproject1build.utility.test.CompanyArticleTest.companyArticleTable;

@Repository
public class CompanyArticleRepositoryJdbc implements CompanyArticleRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CompanyArticleRepositoryJdbc(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<CompanyArticle> findAllArticles() {
        return jdbcTemplate.query("select * from " + companyArticleTable, articleRowMapper());
    }

    @Override
    public Optional<CompanyArticle> searchArticleByName(String name) {
        List<CompanyArticle> oneArticleOrNull = jdbcTemplate.query(
                "select * from " + companyArticleTable + " where name = ?", articleRowMapper(), name);
        return oneArticleOrNull.isEmpty() ? Optional.empty() : Optional.of(oneArticleOrNull.getFirst());
    }

    @Override
    public List<CompanyArticle> searchArticlesByDate(LocalDate date) {
        return jdbcTemplate.query("select * from " + companyArticleTable + " where date = ?", articleRowMapper(), date);
    }

    @Override
    public List<CompanyArticle> searchArticlesByDate(LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.query(
                "select * from " + companyArticleTable + " where date between ? and ?", articleRowMapper(), startDate, endDate);
    }

    @Override
    public Long saveOneArticle(CompanyArticle article) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName(companyArticleTable).usingGeneratedKeyColumns("number");
        Number articleKey = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(article.toMapWithNoNumber()));

        return articleKey.longValue();
    }

    @Override
    public void updateOneArticle(CompanyArticle article) {
        jdbcTemplate.update("update " + companyArticleTable +
                " set press = ?, subjectCompany = ?, link = ?, date = ?, importance = ? where name = ?",
                article.getPress(), article.getSubjectCompany(), article.getLink(), article.getDate(), article.getImportance(),
                article.getName());
    }

    @Override
    public void removeArticleByName(String name) {
        jdbcTemplate.update("delete from " + companyArticleTable + " where name = ?", name);
    }

    /**
     * Other private methods
     */
    private RowMapper<CompanyArticle> articleRowMapper() {
        return (resultSet, rowNumber) ->
                new CompanyArticle.ArticleBuilder()
                        .number(resultSet.getLong("number"))
                        .name(resultSet.getString("name"))
                        .press(resultSet.getString("press"))
                        .subjectCompany(resultSet.getString("subjectcompany"))
                        .link(resultSet.getString("link"))
                        .date(resultSet.getDate("date").toLocalDate())
                        .importance(resultSet.getInt("importance"))
                        .build();
    }
}
