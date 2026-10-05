package com.campusplacement.ui;

import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons;
import com.campusplacement.ui.components.Searchable;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.ui.officer.ApplicationsPage;
import com.campusplacement.ui.officer.CompaniesPage;
import com.campusplacement.ui.officer.DepartmentsPage;
import com.campusplacement.ui.officer.DrivesPage;
import com.campusplacement.ui.officer.JobProfilesPage;
import com.campusplacement.ui.officer.OfferPage;
import com.campusplacement.ui.officer.SkillsPage;
import com.campusplacement.ui.officer.StudentsPage;
import com.campusplacement.ui.student.MyApplicationsPage;
import com.campusplacement.ui.student.MyOffersPage;
import com.campusplacement.ui.student.SelectionStatusPage;
import com.campusplacement.ui.student.StudentDrivesPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Insets;

import static org.assertj.core.api.Assertions.assertThat;

class SearchIntegrationTest {

    @Test
    @DisplayName("HintField guarantees at least 36px left padding when icon glyph is present (no overlap)")
    void testHintFieldNoOverlapInsets() {
        HintField field = Ui.search("Search students...");
        assertThat(field.hasIcon()).isTrue();

        Insets insets = field.getInsets();
        // Icon occupies x=11..26 (15px width). Insets.left must be >= 36 to ensure zero overlap.
        assertThat(insets.left).isGreaterThanOrEqualTo(36);

        // Styling again should not collapse insets
        Ui.style(field);
        Insets insetsAfterStyle = field.getInsets();
        assertThat(insetsAfterStyle.left).isGreaterThanOrEqualTo(36);
    }

    @Test
    @DisplayName("All searchable pages implement Searchable interface and support setSearch/getSearch")
    void testSearchablePagesInterface() {
        assertThat(Searchable.class.isAssignableFrom(StudentsPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(CompaniesPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(DrivesPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(ApplicationsPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(JobProfilesPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(OfferPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(DepartmentsPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(SkillsPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(StudentDrivesPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(MyApplicationsPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(MyOffersPage.class)).isTrue();
        assertThat(Searchable.class.isAssignableFrom(SelectionStatusPage.class)).isTrue();
    }
}
