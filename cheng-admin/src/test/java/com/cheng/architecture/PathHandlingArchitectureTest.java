package com.cheng.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 路徑處理架構規則測試
 *
 * <p>確保專案中 Filter 和 Interceptor 的路徑匹配邏輯
 * 不使用 {@code getRequestURI()}（包含 context path），
 * 避免在外部 Tomcat 部署時因 context path 導致匹配失敗。</p>
 *
 * @author cheng
 * @see com.cheng.common.utils.http.PathUtils
 */
class PathHandlingArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(
                        "com.cheng.common.filter",
                        "com.cheng.shop.security",
                        "com.cheng.framework.interceptor"
                );
    }

    @Test
    @DisplayName("Filter 類別禁止使用 getRequestURI() 進行路徑匹配")
    void filters_should_not_call_getRequestURI() {
        ArchRule rule = noClasses()
                .that()
                .haveSimpleNameEndingWith("Filter")
                .should()
                .callMethod(
                        "jakarta.servlet.http.HttpServletRequest",
                        "getRequestURI"
                )
                .allowEmptyShould(true)
                .because("Filter 的路徑匹配必須使用 PathUtils.getServletPath() 或 request.getServletPath()，"
                        + "因為 getRequestURI() 在外部 Tomcat 部署時會包含 context path（如 /apps），"
                        + "導致路徑比對失敗。詳見 .claude/rules/http-path-handling.md");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Interceptor 類別禁止使用 getRequestURI() 建立快取鍵")
    void interceptors_should_not_call_getRequestURI() {
        ArchRule rule = noClasses()
                .that()
                .haveSimpleNameEndingWith("Interceptor")
                .should()
                .callMethod(
                        "jakarta.servlet.http.HttpServletRequest",
                        "getRequestURI"
                )
                .allowEmptyShould(true)
                .because("Interceptor 的快取鍵建立必須使用 PathUtils.getServletPath()，"
                        + "確保不同部署環境下快取鍵結構一致。");

        rule.check(importedClasses);
    }
}
